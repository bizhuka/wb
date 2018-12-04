package com.modekz;

import com.google.gson.Gson;
import com.modekz.json.ConnRfc;
import com.modekz.json.ConnVCAP;
import com.modekz.rfc.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.apache.commons.lang.SystemUtils;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.hibersap.configuration.AnnotationConfiguration;
import org.hibersap.configuration.xml.SessionManagerConfig;
import org.hibersap.session.SessionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ODataServiceFactory extends ODataJPAServiceFactory {

    /**
     * The package that contains all the model classes.
     */
    private static final String PERSISTENCE_UNIT_NAME = "com.modekz.db";
    private static EntityManagerFactory emf;
    private static SessionManager rfcSessionManager;

    public static EntityManagerFactory getEmf() throws ServletException {
        if (emf == null)
            try {
                Map<String, String> properties = new HashMap<>();
                Gson gson = new Gson();

                String persistenceUnitName;
                ConnVCAP.Credentials credentials = null;

                // Read DB connection options
                if (SystemUtils.IS_OS_WINDOWS) {
                    persistenceUnitName = "postgre-unit";
                    credentials = gson.fromJson(System.getenv("WB_DB_UNIT"), ConnVCAP.Credentials.class);
                } else {
                    persistenceUnitName = "hana-unit";
                    ConnVCAP vcap = gson.fromJson(System.getenv("VCAP_SERVICES"), ConnVCAP.class);
                    for (ConnVCAP.Hana hana : vcap.hana)
                        if (hana.name.startsWith("wb-")) {
                            credentials = hana.credentials;
                            break;
                        }
                }

                // From json
                if (credentials != null) {
                    // properties.put("javax.persistence.jdbc.driver", );
                    properties.put("javax.persistence.jdbc.url", credentials.url);
                    properties.put("javax.persistence.jdbc.user", credentials.user);
                    properties.put("javax.persistence.jdbc.password", credentials.password);
                }

                emf = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException(e);
            }
        return emf;
    }

    public static Connection getConnection(EntityManager em) {
        return em.unwrap(java.sql.Connection.class);
    }

    public static SessionManager getRfcSession() {
        if (rfcSessionManager == null) {
            // Read from json
            Gson gson = new Gson();
            ConnRfc connRfc = gson.fromJson(System.getenv("WB_RFC_DEST"), ConnRfc.class);

            SessionManagerConfig cfg = new SessionManagerConfig("A12")
                    .setProperty(DestinationDataProvider.JCO_ASHOST, connRfc.ashost)
                    .setProperty(DestinationDataProvider.JCO_SYSNR, connRfc.sysnr)
                    .setProperty(DestinationDataProvider.JCO_CLIENT, connRfc.client)
                    .setProperty(DestinationDataProvider.JCO_USER, connRfc.user)
                    .setProperty(DestinationDataProvider.JCO_PASSWD, connRfc.passwd)
                    .setProperty(DestinationDataProvider.JCO_LANG, connRfc.lang)
                    .setProperty(DestinationDataProvider.JCO_SAPROUTER, connRfc.saprouter);

            AnnotationConfiguration configuration = new AnnotationConfiguration(cfg);
            configuration.addBapiClasses(
                    WBRead.class, WBSetStatus.class, WBPrintDoc.class, MeasureDoc.class, WlnVehicleFm.class);
            rfcSessionManager = configuration.buildSessionManager();
        }
        return rfcSessionManager;
    }

    @Override
    public ODataJPAContext initializeODataJPAContext()
            throws ODataJPARuntimeException {

        ODataJPAContext oDataJPAContext = this.getODataJPAContext();
        try {
            oDataJPAContext.setEntityManagerFactory(getEmf());
            oDataJPAContext.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
//            oDataJPAContext.setJPAEdmExtension(new EspmProcessingExtension());
//            oDataJPAContext.setJPAEdmMappingModel("oDataMapping.xml");
            return oDataJPAContext;
        } catch (Exception e) {
            throw new ODataRuntimeException(e);
        }
    }
}
