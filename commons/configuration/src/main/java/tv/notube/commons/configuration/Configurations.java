package tv.notube.commons.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.configuration.analytics.AnalyticsConfiguration;
import tv.notube.commons.configuration.analytics.MethodDescription;
import tv.notube.commons.configuration.auth.ServiceAuthorizationManagerConfiguration;
import tv.notube.commons.configuration.profiler.*;
import tv.notube.commons.configuration.storage.StorageConfiguration;
import tv.notube.commons.configuration.usermanager.UserManagerConfiguration;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.Service;
import tv.notube.commons.storage.model.Query;
import tv.notube.commons.storage.model.QueryException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Configurations {

    public static synchronized <T extends Configuration> T getConfiguration(
            String filePath,
            Class<T> serviceClass
    ) throws ConfigurationsException {

        if (filePath == null)
            throw new IllegalArgumentException("Configuration file path cannot be null");

        XMLConfiguration xmlConfiguration;
        try {
            xmlConfiguration = new XMLConfiguration(
                    Configurations.class
                            .getClassLoader()
                            .getResource(filePath)
            );
        } catch (ConfigurationException e) {
            final String errMsg = "Error while loading XMLConfiguration";
            throw new RuntimeException(errMsg, e);
        }
        return parse(xmlConfiguration, serviceClass);
    }

    private static <T extends Configuration> T parse(
            XMLConfiguration xmlConfiguration,
            Class<T> serviceClass
    ) throws ConfigurationsException {
        if (serviceClass.equals(UserManagerConfiguration.class)) {
            return userManagerConfiguration(xmlConfiguration);
        }
        if (serviceClass.equals(AnalyticsConfiguration.class)) {
            return analyticsConfiguration(xmlConfiguration);
        }
        if (serviceClass.equals(ProfilerConfiguration.class)) {
            return profilerConfiguration(xmlConfiguration);
        }
        if (serviceClass.equals(ServiceAuthorizationManagerConfiguration.class)) {
            return samConfiguration(xmlConfiguration);
        }
        if (serviceClass.equals(StorageConfiguration.class)) {
            return storageConfiguration(xmlConfiguration);
        }
        final String errMsg = "Class [" + serviceClass + "] is not supported";
        throw new ConfigurationsException(errMsg);
    }

    private static <T extends Configuration> T storageConfiguration(XMLConfiguration xmlConfiguration) {
        HierarchicalConfiguration kvs = xmlConfiguration.configurationAt("kvs");
        String kvsHostStr = kvs.getString("host");
        String kvsPortStr = kvs.getString("port");
        String kvsDbStr = kvs.getString("db");
        String kvsUserStr = kvs.getString("user");
        String kvsPasswdStr = kvs.getString("password");

        Properties kvsProps = new Properties();
        kvsProps.setProperty("url", "jdbc:mysql://" + kvsHostStr + ":" + kvsPortStr + "/" + kvsDbStr);
        kvsProps.setProperty("username", kvsUserStr);
        kvsProps.setProperty("password", kvsPasswdStr);

        HierarchicalConfiguration alog = xmlConfiguration.configurationAt("alog");
        String alogHostStr = alog.getString("host");
        String alogPortStr = alog.getString("port");
        String alogDbStr = alog.getString("db");
        String alogUserStr = alog.getString("user");
        String alogPasswdStr = alog.getString("password");

        Properties alogProps = new Properties();
        alogProps.setProperty("url", "jdbc:mysql://" + alogHostStr + ":" + alogPortStr + "/" + alogDbStr);
        alogProps.setProperty("username", alogUserStr);
        alogProps.setProperty("password", alogPasswdStr);

        return (T) new StorageConfiguration(alogProps, kvsProps);
    }

    private static <T extends Configuration> T samConfiguration(XMLConfiguration xmlConfiguration) {
        List<HierarchicalConfiguration> serviceConfs = xmlConfiguration
                .configurationsAt("service");
        ServiceAuthorizationManagerConfiguration samc =
                new ServiceAuthorizationManagerConfiguration();
        for (HierarchicalConfiguration serviceConf : serviceConfs) {
            String name = serviceConf.getString("[@name]");
            String handler = serviceConf.getString("[@handler]");
            String description = serviceConf.getString("description");
            String apikey = serviceConf.getString("apikey");
            String secret = serviceConf.getString("secret");
            String session = serviceConf.getString("session");
            String endpoint = serviceConf.getString("endpoint").replace("&amp;", "&");
            Service service = new Service(name);
            service.setApikey(apikey);
            service.setDescription(description);
            service.setSecret(secret);
            try {
                service.setEndpoint(new URL(endpoint));
            } catch (MalformedURLException e) {
                final String errMsg = "endpoint for service [" + name + "]" +
                        "is not well-formed";
                throw new RuntimeException(errMsg, e);
            }
            if (session.equals("")) {
                service.setSessionEndpoint(null);
            } else {
                try {
                    service.setSessionEndpoint(new URL(session));
                } catch (MalformedURLException e) {
                    final String errMsg = "session for service [" + name + "]" +
                            "is not well-formed";
                    throw new RuntimeException(errMsg, e);
                }
            }
            Class<? extends AuthHandler> handlerClass = getHandler(handler);
            samc.addService(service, handlerClass);
        }
        return (T) samc;
    }

    private static <T extends Configuration> T profilerConfiguration(XMLConfiguration xmlConfiguration) {
        ProfilerConfiguration profilerConfiguration = new
                ProfilerConfiguration();
        try {
            initDataManager(profilerConfiguration, xmlConfiguration);
        } catch (DataManagerConfigurationException e) {
            final String errMsg = "Error while configuring the data manager";
            throw new RuntimeException(errMsg, e);
        }
        initProfileStore(profilerConfiguration, xmlConfiguration);
        initProfilingLines(profilerConfiguration, xmlConfiguration);
        checkKeysIntegrity(profilerConfiguration);
        return (T) profilerConfiguration;
    }

    private static void checkKeysIntegrity(
            ProfilerConfiguration profilerConfiguration
    ) {
        ProfileStoreConfiguration profileStoreConfiguration =
                profilerConfiguration.getProfileStoreConfiguration();
        Map<String, List<String>> registeredKeys
                = profilerConfiguration.getDataManagerConfiguration().getRegisteredKeys();
        Set<String> psKeys = profileStoreConfiguration.getNameSpacesConfiguration().keySet();
        Set<String> dmKeys =  registeredKeys.keySet();
        if(psKeys.size() != dmKeys.size()) {
            throw new RuntimeException("Error: keys declared for Data Manager are not" +
                    "the same number of the ones declared by the ProfileStore");
        }
        if(!psKeys.equals(dmKeys)) {
            throw new RuntimeException("Error: keys declared for Data Manager are not" +
                    "the same");
        }
    }

    private static void initProfilingLines(
            ProfilerConfiguration profilerConfiguration,
            XMLConfiguration xmlConfiguration
    ) {
        List<HierarchicalConfiguration> plines =
                xmlConfiguration.configurationsAt("profiling-lines.profiling-line");
        for(HierarchicalConfiguration pline : plines) {
            String name = pline.getString("name");
            String description = pline.getString("description");
            String clazz = pline.getString("class");
            List<HierarchicalConfiguration> plineItems =
                    pline.configurationsAt("profiling-line-items.profiling-line-item");
            List<ProfilingLineItemDescription> itemsDescriptions
                    = new ArrayList<ProfilingLineItemDescription>();
            for(HierarchicalConfiguration plineItem : plineItems) {
                String itemName = plineItem.getString("name");
                String itemDescription = plineItem.getString("description");
                String itemClazz = plineItem.getString("class");
                List<HierarchicalConfiguration> parameters =
                        plineItem.configurationsAt("parameters");
                Map<String, String> parametersMap = new HashMap<String, String>();
                if(parameters != null) {
                    for (HierarchicalConfiguration parameter : parameters) {
                        String parameterName = parameter.getString("parameter.name");
                        String parameterValue = parameter.getString("parameter.value");
                        parametersMap.put(parameterName, parameterValue);
                    }
                }
                ProfilingLineItemDescription plid = new ProfilingLineItemDescription(
                        itemName,
                        itemDescription,
                        itemClazz,
                        parametersMap
                );
                itemsDescriptions.add(plid);
            }
            ProfilingLineDescription pld = new ProfilingLineDescription(
                    name,
                    description,
                    clazz,
                    itemsDescriptions
            );
            profilerConfiguration.add(pld);
        }
    }

    private static void initProfileStore(
            ProfilerConfiguration profilerConfiguration,
            XMLConfiguration xmlConfiguration
    ) {
        HierarchicalConfiguration pstore = xmlConfiguration.configurationAt("profile-store");
        Map<String, String> nameSpacesConfiguration = new HashMap<String, String>();

        List<HierarchicalConfiguration> keys = pstore.configurationsAt("tables.table");
        for (HierarchicalConfiguration profileStore : keys) {
            String keyName = profileStore.getString("[@name]");
            String table = profileStore.getString("");
            nameSpacesConfiguration.put(keyName, table);
        }

        ProfileStoreConfiguration profileStoreConfiguration =
                new ProfileStoreConfiguration(nameSpacesConfiguration);
        profilerConfiguration.setProfileStoreConfiguration(profileStoreConfiguration);
    }

    private static void initDataManager(ProfilerConfiguration profilerConfiguration, XMLConfiguration xmlConfiguration)
            throws DataManagerConfigurationException {
        DataManagerConfiguration dataManagerConfiguration = new DataManagerConfiguration();
        HierarchicalConfiguration dmc = xmlConfiguration.configurationAt("data-manager");
        List<HierarchicalConfiguration> keys = dmc.configurationsAt("keys.key");
        for (HierarchicalConfiguration key : keys) {
            String keyString = key.getString("[@name]");
            String dataSourceClassName = key.getString("[@class]");
            List<HierarchicalConfiguration> lines = key.configurationsAt("lines");
            for (HierarchicalConfiguration line : lines) {
                String lineNameString = line.getString("line");
                dataManagerConfiguration.registerKey(
                        keyString,
                        lineNameString,
                        dataSourceClassName
                );
            }
        }
        profilerConfiguration.setDataManagerConfiguration(dataManagerConfiguration);
    }

    private static <T extends Configuration> T analyticsConfiguration(XMLConfiguration xmlConfiguration) {
        List<HierarchicalConfiguration> analyses =
                xmlConfiguration.configurationsAt("analyses.analysis");
        Set<AnalysisDescription> analysisDescriptions =
            new HashSet<AnalysisDescription>();
        for(HierarchicalConfiguration analysis : analyses) {
            String name = analysis.getString("[@name]");
            String classStr = analysis.getString("[@class]");
            String description = analysis.getString("description");
            String queryStr = analysis.getString("query");
            String resultClass = analysis.getString("result");
            Query query = new Query();
            try {
                Query.decompile(queryStr, query);
            } catch (QueryException e) {
                final String errMsg = "Query for analysis [" + name + "] has " +
                        "syntax errors";
                throw new RuntimeException(errMsg, e);
            }
            Set<MethodDescription> mds = new HashSet<MethodDescription>();
            List<HierarchicalConfiguration> methods = analysis.configurationsAt("methods.method");
            for(HierarchicalConfiguration method : methods) {
                String methodName = method.getString("[@name]");
                String methodDescriptionStr = method.getString("description");
                List<HierarchicalConfiguration> parameterTypes = method
                        .configurationsAt("parameterTypes");
                List<String> paramTypesStr = new ArrayList<String>();
                for(HierarchicalConfiguration parameterType : parameterTypes){
                    String paramType = parameterType.getString("type");
                    paramTypesStr.add(paramType);
                }
                MethodDescription methodDescription = new MethodDescription(
                        methodName,
                        methodDescriptionStr,
                        paramTypesStr.toArray(new String[paramTypesStr.size()])
                );
                mds.add(methodDescription);
            }
            AnalysisDescription analysisDescription =
                    new AnalysisDescription(
                            name,
                            description,
                            query,
                            classStr,
                            resultClass,
                            mds
                    );
            analysisDescriptions.add(analysisDescription);
        }
        return (T) new AnalyticsConfiguration(analysisDescriptions);
    }

    private static <T extends Configuration> T userManagerConfiguration(XMLConfiguration xmlConfiguration) {
        long rate = xmlConfiguration.getLong("profiling-rate");
        UserManagerConfiguration userManagerConfiguration = new
                UserManagerConfiguration(rate);
        return (T) userManagerConfiguration;
    }

    private static Class<? extends AuthHandler> getHandler(String handler) {
        Class<? extends AuthHandler> handlerClass;
        try {
            handlerClass = (Class<? extends AuthHandler>) Class.forName(handler);
        } catch (ClassNotFoundException e) {
            final String errMsg = "Class [" + handler + "] not found";
            throw new RuntimeException(errMsg, e);
        }
        return handlerClass;
    }


}
