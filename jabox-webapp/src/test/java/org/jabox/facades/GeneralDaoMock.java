package org.jabox.facades;

import java.util.List;

import org.apache.wicket.persistence.provider.GeneralDao;
import org.jabox.apis.IBaseEntity;
import org.jabox.cis.hudson.HudsonConnectorConfig;
import org.jabox.model.DefaultConfiguration;
import org.jabox.model.Server;

public class GeneralDaoMock implements GeneralDao {

	private final DefaultConfiguration _dc;

	public GeneralDaoMock() {
		_dc = new DefaultConfiguration();

		// Mock CIS
		HudsonConnectorConfig cis = new HudsonConnectorConfig();
		cis.server = new Server();
		cis.server.setName("HudsonMock");
		_dc.setDefault(cis);
	}

	public void deleteEntity(IBaseEntity entity) {

	}

	public <T extends IBaseEntity> T findEntity(Long id, Class<T> clazz) {
		return null;
	}

	public DefaultConfiguration getDefaultConfiguration() {
		return _dc;
	}

	public <T extends IBaseEntity> List<T> getEntities(Class<T> clazz) {
		return null;
	}

	public <T extends IBaseEntity> void persist(T object) {
	}
}