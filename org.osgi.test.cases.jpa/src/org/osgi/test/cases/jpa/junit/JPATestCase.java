/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/

package org.osgi.test.cases.jpa.junit;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.test.support.compatibility.DefaultTestBundleControl;
import org.osgi.test.support.tracker.Tracker;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class JPATestCase extends DefaultTestBundleControl {

    public static final long SERVICE_WAIT_TIME = 5000;

	public void testPersistenceClass() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("staticAccessBundle.jar");
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactory.class);
			emf = Persistence.createEntityManagerFactory("staticAccessTestUnit");
			assertNotNull("Unable to create the specified EntityManagerFactory", emf);
		} finally {
			if (emf != null) {
				emf.close();
			}
			uninstallBundle(persistenceBundle);
		}

	}

	public void testPersistenceClassWithMap() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("staticAccessWithMapBundle.jar");
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactoryBuilder.class);
			DataSourceFactory dsf = getService(DataSourceFactory.class);
			ServiceReference< ? > dsfRef = getServiceReference(dsf);
			assertNotNull("Unable to retrieve a reference for the DataSourceFactory service", dsfRef);
			Map<String,Object> props = new HashMap<>();
			props.put("javax.persistence.jdbc.driver", dsfRef.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
			emf = Persistence.createEntityManagerFactory("staticAccessWithMapTestUnit", props);
			assertNotNull("Unable to create the specified EntityManagerFactory", emf);
		} finally {
			if (emf != null) {
				emf.close();
			}
			uninstallBundle(persistenceBundle);
		}
	}

	public void testEntityManagerFactory() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("emfBundle.jar");
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactory.class);
			emf = getService(EntityManagerFactory.class, "(osgi.unit.name=emfTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactory", emf);
		} finally {
			if (emf != null) {
				emf.close();
				ungetService(emf);
			}
			uninstallBundle(persistenceBundle);
		}

	}

	public void testEntityManagerFactoryWithIncompletePersistenceUnit() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("incompletePersistenceUnitBundle.jar");
		try {
			waitForService(EntityManagerFactoryBuilder.class);
			ServiceReference< ? >[] emfRefs = getContext().getServiceReferences(
					EntityManagerFactory.class.getName(),
					"(osgi.unit.name=incompleteTestUnit)");
			assertNull("There should be no EntityManagerFactory registered since this persistence unit is incomplete", emfRefs);
		} finally {
			uninstallBundle(persistenceBundle);
		}
	}

	public void testEntityManagerFactoryBuilder() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("emfBuilderBundle.jar");
		EntityManagerFactoryBuilder emfBuilder = null;
		@SuppressWarnings("unused")
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactoryBuilder.class);
			emfBuilder = getService(EntityManagerFactoryBuilder.class, "(osgi.unit.name=emfBuilderTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactoryBuilder", emfBuilder);
			DataSourceFactory dsf = getService(DataSourceFactory.class);
			ServiceReference< ? > dsfRef = getServiceReference(dsf);
			assertNotNull("Unable to retrieve a reference for the DataSourceFactory service", dsfRef);
			Map<String,Object> props = new HashMap<>();
			props.put("javax.persistence.jdbc.driver", dsfRef.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
			props.put("fake.property", "fake property value");
			emf = emfBuilder.createEntityManagerFactory(props);
		} catch (java.lang.IllegalArgumentException ex) {
			fail("Unknown properties should be ignored and not result in an IllegalArgumentException.");
		} finally {
			if (emfBuilder != null) {
				ungetService(emfBuilder);
			}
			uninstallBundle(persistenceBundle);
		}
	}

	public void testEntityManagerFactoryBuilderRebinding() throws Exception {
		// Install the bundle necessary for this test
		Bundle persistenceBundle = installBundle("emfBuilderRebindingBundle.jar");
		EntityManagerFactoryBuilder emfBuilder = null;
		@SuppressWarnings("unused")
		EntityManagerFactory emf1 = null;
		@SuppressWarnings("unused")
		EntityManagerFactory emf2 = null;
		try {
			waitForService(EntityManagerFactoryBuilder.class);
			emfBuilder = getService(EntityManagerFactoryBuilder.class, "(osgi.unit.name=emfBuilderRebindingTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactroyBuilder", emfBuilder);
			DataSourceFactory dsf = getService(DataSourceFactory.class);
			ServiceReference< ? > dsfRef = getServiceReference(dsf);
			assertNotNull("Unable to retrieve a reference for the DataSourceFactory service", dsfRef);
			Map<String,Object> props1 = new HashMap<>();
			props1.put("javax.persistence.jdbc.driver", dsfRef.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
			emf1 = emfBuilder.createEntityManagerFactory(props1);
			Map<String,Object> props2 = new HashMap<>();
			props2.put("javax.persistence.jdbc.driver", "fake.driver.class");
			emf2 = emfBuilder.createEntityManagerFactory(props2);
		} catch (java.lang.IllegalArgumentException ex) {
			pass("java.lang.IllegalArgumentException caught in testEntityManagerFactoryBuilderRebinding: SUCCESS");
			return;
		} finally {
			if (emfBuilder != null) {
				ungetService(emfBuilder);
			}
			uninstallBundle(persistenceBundle);
		}
		failException("testEntityManagerFactoryBuilderRebinding failed", java.lang.IllegalArgumentException.class);
	}

	public void testEntityManagerFactoryRebindingWithBuilder() throws Exception {
		// Install the bundle necessary for this test
		Bundle persistenceBundle = installBundle("emfRebindingWithBuilderBundle.jar");
		EntityManagerFactoryBuilder emfBuilder = null;
		EntityManagerFactory emf1 = null;
		EntityManagerFactory emf2 = null;
		try {
			waitForService(EntityManagerFactory.class);
			waitForService(EntityManagerFactoryBuilder.class);
			emf1 = getService(EntityManagerFactory.class, "(osgi.unit.name=emfRebindingWithBuilderTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactory", emf1);
			assertTrue(emf1.isOpen());

			emfBuilder = getService(EntityManagerFactoryBuilder.class, "(osgi.unit.name=emfRebindingWithBuilderTestUnit)");
			Map<String,Object> props = new HashMap<>();
			props.put("javax.persistence.jdbc.driver", "fake.driver.class");
			emf2 = emfBuilder.createEntityManagerFactory(props);
		} catch (java.lang.IllegalArgumentException ex) {
			pass("java.lang.IllegalArgumentException caught in testEntityManagerFactoryRebindingWithBuilder: SUCCESS");
			return;
		} finally {
			if (emf1 != null) {
				emf1.close();
				ungetService(emf1);
			}
			if (emf2 != null) {
				emf2.close();
			}
			if (emfBuilder != null) {
				ungetService(emfBuilder);
			}
			uninstallBundle(persistenceBundle);
		}
		failException("testEntityManagerFactoryRebindingWithBuilder failed", java.lang.IllegalArgumentException.class);

	}

	public void testConfigPropertiesWithEntityManagerFactoryBuilder() throws Exception {
		// Install the bundle necessary for this test
		Bundle persistenceBundle = installBundle("configPropertiesBundle.jar");
		EntityManagerFactoryBuilder emfBuilder = null;
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactoryBuilder.class);
			emfBuilder = getService(EntityManagerFactoryBuilder.class, "(osgi.unit.name=configPropertiesTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactoryBuilder.", emfBuilder);
			DataSourceFactory dsf = getService(DataSourceFactory.class);
			ServiceReference< ? > dsfRef = getServiceReference(dsf);
			assertNotNull("Unable to retrieve a reference for the DataSourceFactory service", dsfRef);
			Map<String,Object> props = new HashMap<>();
			props.put("javax.persistence.jdbc.driver", dsfRef.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
			props.put("javax.persistence.jdbc.password", "configPassword");
			emf = emfBuilder.createEntityManagerFactory(props);
		} finally {
			if (emfBuilder != null) {
				ungetService(emfBuilder);
			}
			if (emf != null) {
				emf.close();
			}
			uninstallBundle(persistenceBundle);
		}
	}


	public void testDataSourceFactoryUnregistration() throws Exception {
		// Install the bundle needed for this test.
		Bundle emfBundle = installBundle("dsfEMFBundle.jar");
		Bundle dsfBundle = null;
		EntityManagerFactoryBuilder emfBuilder = null;
		try {
			waitForService(EntityManagerFactory.class);
			DataSourceFactory dsf = getService(DataSourceFactory.class);
			ServiceReference< ? > dsfRef = getServiceReference(dsf);
			ungetService(dsf);
			dsfBundle = dsfRef.getBundle();
			dsfBundle.stop();
			// Make sure the entityManagerFactory is no longer available.
			ServiceReference< ? >[] emfRef = getContext().getServiceReferences(
					EntityManagerFactory.class.getName(),
					"(osgi.unit.name=dsfEMFTestUnit)");
			assertNull("There should be no entityManagerFactory service registered for this persistence unit", emfRef);
			// The emfBuilder service should not have been unregistered as its lifecycle is supposed to be independent of the dsf
			emfBuilder = getService(EntityManagerFactoryBuilder.class, "(osgi.unit.name=dsfEMFTestUnit)");
			assertNotNull(emfBuilder);
		} finally {
			if (emfBuilder != null) {
				ungetService(emfBuilder);
			}
			dsfBundle.start();
			uninstallBundle(emfBundle);
		}
	}

	public void testPersistenceBundleStopping() throws Exception {
		// Install the bundles necessary for this test
		Bundle persistenceBundle = installBundle("emfBundle.jar");
		EntityManagerFactory emf = null;
		try {
			waitForService(EntityManagerFactory.class);
			emf = getService(EntityManagerFactory.class, "(osgi.unit.name=emfTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactory", emf);
			uninstallBundle(persistenceBundle);
			try {
				if (emf.isOpen()) {
					fail("The EntityManagerFactory should have been closed when the persistence bundle was uninstalled");
				}
			} catch (NullPointerException npe) {
				// Do nothing.  An NPE is expected if the bundle performs all the appropriate steps when stopping.
			}
		} finally {
			if (persistenceBundle.getState() != Bundle.UNINSTALLED) {
				uninstallBundle(persistenceBundle);
			}
		}
		// Reinstall the bundle and go through the motions again to make sure nothing that conflicts was left behind
		Bundle reinstalledBundle = installBundle("emfBundle.jar");
		emf = null;
		try {
			waitForService(EntityManagerFactory.class);
			emf = getService(EntityManagerFactory.class, "(osgi.unit.name=emfTestUnit)");
			assertNotNull("Unable to retrieve the specified EntityManagerFactory", emf);
		} finally {
			uninstallBundle(reinstalledBundle);
		}

	}

	public void testPersistenceProviderRegistration() throws Exception {
		// We should already have a provider present in the registry.  Make sure we can grab it.
		PersistenceProvider provider = getService(PersistenceProvider.class);
		assertNotNull("The PersistenceProvider service should be registered when the JPA Provider is installed", provider);
		// The javax.persistence.provider property should have been registered alongside the PersistenceProvider service
		ServiceReference< ? > providerRef = getServiceReference(provider);
		String javaxPersistenceProvider = (String) providerRef.getProperty("javax.persistence.provider");
		assertNotNull("The javax.persistence.provider service property should be registered alongside the PersistenceProvider service", javaxPersistenceProvider);
	}

	public <S> S waitForService(Class<S> cls) {
		ServiceTracker<S,S> tracker = new ServiceTracker<>(getContext(), cls,
				null);
        tracker.open();
		S service = null;
		try {
			service = Tracker.waitForService(tracker, SERVICE_WAIT_TIME);
		}
		catch (InterruptedException intEx) {
			// service will be null
		}
        tracker.close();
        assertNotNull("Service for " + cls.getName() + " was not registered after waiting " +
            SERVICE_WAIT_TIME + " milliseconds", service);
		return service;
    }
}
