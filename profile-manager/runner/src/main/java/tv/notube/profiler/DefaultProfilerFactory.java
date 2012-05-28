package tv.notube.profiler;

import tv.notube.commons.configuration.Configurations;
import tv.notube.commons.configuration.ConfigurationsException;
import tv.notube.commons.configuration.profiler.*;
import tv.notube.commons.configuration.storage.StorageConfiguration;
import tv.notube.commons.storage.kvs.KVStore;
import tv.notube.commons.storage.kvs.mybatis.MyBatisKVStore;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;
import tv.notube.profiler.container.DefaultProfilingLineContainer;
import tv.notube.profiler.container.ProfilingLineContainer;
import tv.notube.profiler.container.ProfilingLineContainerException;
import tv.notube.profiler.data.DataManager;
import tv.notube.profiler.data.DataManagerException;
import tv.notube.profiler.data.ModularDataManager;
import tv.notube.profiler.line.ProfilingLine;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.storage.KVProfileStoreImpl;
import tv.notube.profiler.storage.ProfileStore;
import tv.notube.usermanager.DefaultUserManagerFactory;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerFactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultProfilerFactory {

    private final static String PROFILER_CONF = "profiler-configuration.xml";

    private static DefaultProfilerFactory instance;

    public static DefaultProfilerFactory getInstance() {
        if (instance == null) {
            instance = new DefaultProfilerFactory();
        }
        return instance;
    }

    private DefaultProfilerImpl profiler;

    private DefaultProfilerFactory() {
        UserManager um;
        try {
            um = DefaultUserManagerFactory.getInstance().build();
        } catch (UserManagerFactoryException e) {
            final String errMsg = "Error while getting the user manager";
            throw new RuntimeException(errMsg, e);
        }
        ProfilerConfiguration pc;
        try {
            pc = Configurations.getConfiguration(
                    PROFILER_CONF,
                    ProfilerConfiguration.class
            );
        } catch (ConfigurationsException e) {
            final String errMsg = "Error while getting configuration for profiler";
            throw new RuntimeException(errMsg, e);
        }
        DataManagerConfiguration dmc = pc.getDataManagerConfiguration();
        ProfileStoreConfiguration psc = pc.getProfileStoreConfiguration();

        DataManager dm;
        try {
            dm = getDataManager(um, dmc);
        } catch (DataManagerException e) {
            final String errMsg = "Error while getting the data manager";
            throw new RuntimeException(errMsg, e);
        }
        ProfilingLineContainer plc = getProfilingLineContainer(pc.getPlDescriptions());
        ProfileStore ps;
        try {
            ps = getProfileStore(psc);
        } catch (ConfigurationsException e) {
            final String errMsg = "Error while getting the profile store";
            throw new RuntimeException(errMsg, e);
        }
        profiler = new DefaultProfilerImpl(
                dm,
                plc,
                ps
        );
    }

    private DataManager getDataManager(
            UserManager um,
            DataManagerConfiguration dmc
    ) throws DataManagerException {
        return new ModularDataManager(um, dmc);
    }

    private ProfilingLineContainer getProfilingLineContainer(List<ProfilingLineDescription> plDescriptions) {
        ProfilingLineContainer plc = new DefaultProfilingLineContainer();
        for (ProfilingLineDescription profilingLineDescription : plDescriptions) {
            String plName = profilingLineDescription.getName();
            String plDescription = profilingLineDescription.getDescription();
            Class<? extends ProfilingLine> clazz;
            try {
                clazz = (Class<? extends ProfilingLine>) Class.forName(profilingLineDescription.getClazz());
            } catch (ClassNotFoundException e) {
                final String errMsg = "Class [" + profilingLineDescription
                        .getClazz() + "] not found";
                throw new RuntimeException(errMsg, e);
            }
            Constructor c;
            try {
                c = clazz.getConstructor(String.class, String.class);
            } catch (NoSuchMethodException e) {
                final String errMsg = "Constructor for class [" + clazz.getName() +
                        "] not found";
                throw new RuntimeException(errMsg, e);
            }
            ProfilingLine profilingLine;
            try {
                profilingLine = (ProfilingLine) c.newInstance(plName, plDescription);
            } catch (InstantiationException e) {
                throw new RuntimeException(
                        "Error while instantiating profiling line [" + plName + "]",
                        e
                );
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Error while accessing profiling line [" + plName + "]",
                        e
                );
            } catch (InvocationTargetException e) {
                throw new RuntimeException(
                        "Error while invokating profiling line [" + plName + "]",
                        e
                );
            }
            for (ProfilingLineItemDescription profilingLineItemDescription :
                    profilingLineDescription.getPliDescriptions()) {
                String pliName = profilingLineItemDescription.getName();
                String pliDescr = profilingLineItemDescription.getDescription();
                Class<? extends ProfilingLineItem> pliClazz;
                try {
                    pliClazz = (Class<? extends ProfilingLineItem>) Class.forName(profilingLineItemDescription.getClazz());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Profiling line class [" +
                            profilingLineItemDescription.getClazz() + "] not found",
                            e
                    );
                }
                Constructor pliC;
                if (profilingLineItemDescription.getParameters().size() == 0) {
                    try {
                        pliC = pliClazz.getConstructor(String.class, String.class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Constructor not found", e);
                    }
                    profilingLine.enqueueProfilingLineItem(
                            getPLI(
                                    pliName,
                                    pliDescr,
                                    pliC
                            )
                    );
                } else {
                    try {
                        pliC = pliClazz.getConstructor(
                                String.class,
                                String.class,
                                Map.class
                        );
                        profilingLine.enqueueProfilingLineItem(
                                getPLI(
                                        pliName,
                                        pliDescr,
                                        pliC,
                                        profilingLineItemDescription.getParameters()
                                )
                        );
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Constructor not found", e);
                    }
                }

            }
            try {
                plc.addProfilingLine(profilingLine);
            } catch (ProfilingLineContainerException e) {
                final String errMsg = "Error while adding line to the container";
                throw new RuntimeException(errMsg, e);
            }
        }
        return plc;
    }

    private ProfilingLineItem getPLI(
            String pliName,
            String pliDescr,
            Constructor pliC,
            Map<String, String> parameters
    ) {
        ProfilingLineItem profilingLineItem;
        try {
            profilingLineItem = (ProfilingLineItem)
                    pliC.newInstance(pliName, pliDescr, parameters);
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        }
        return profilingLineItem;
    }

    private ProfilingLineItem getPLI(String pliName, String pliDescr, Constructor pliC) {
        ProfilingLineItem profilingLineItem;
        try {
            profilingLineItem = (ProfilingLineItem)
                    pliC.newInstance(pliName, pliDescr);
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Instantiation error for [" + pliName + "]",
                    e
            );
        }
        return profilingLineItem;
    }


    private ProfileStore getProfileStore(ProfileStoreConfiguration psc) throws ConfigurationsException {
        StorageConfiguration storageConfiguration = Configurations.getConfiguration(
                "storage-configuration.xml",
                StorageConfiguration.class
        );
        Properties p = storageConfiguration.getKvsProperties();
        KVStore kvs = new MyBatisKVStore(p, new SerializationManager());
        ProfileStore profileStore = new KVProfileStoreImpl(kvs);
        profileStore.setNamespaces(psc.getNameSpacesConfiguration());
        return profileStore;
    }

    public DefaultProfilerImpl build() {
        return profiler;
    }


}
