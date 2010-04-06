/*
 * Jabox Open Source Version
 * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
 * 
 * This file is part of Jabox
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.jabox.application;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.reactor.MavenExecutionException;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.persistence.provider.GeneralDao;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jabox.apis.Manager;
import org.jabox.apis.cis.CISConnector;
import org.jabox.apis.its.ITSConnector;
import org.jabox.apis.its.ITSConnectorConfig;
import org.jabox.apis.rms.RMSConnector;
import org.jabox.apis.scm.SCMConnector;
import org.jabox.apis.scm.SCMConnectorConfig;
import org.jabox.apis.scm.SCMException;
import org.jabox.model.DefaultConfiguration;
import org.jabox.model.Project;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

public class CreateProjectUtil {
	@SpringBean
	protected GeneralDao generalDao;

	@SpringBean
	protected Manager<ITSConnector> _itsManager;

	@SpringBean
	protected Manager<SCMConnector<SCMConnectorConfig>> _scmManager;

	@SpringBean
	protected Manager<CISConnector> _cisManager;

	@SpringBean
	protected Manager<RMSConnector> _rmsManager;

	public CreateProjectUtil() {
		InjectorHolder.getInjector().inject(this);
	}

	public void createProject(final Project project) {
		try {
			createProjectMethod(project);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRepositoryException e) {
			e.printStackTrace();
		} catch (MavenExecutionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (SCMException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param project
	 * @throws IOException
	 * @throws InvalidRepositoryException
	 * @throws MavenExecutionException
	 * @throws SVNException
	 * @throws SAXException
	 * @throws SCMException
	 * @throws IOException
	 */
	private void createProjectMethod(final Project project)
			throws InvalidRepositoryException, MavenExecutionException,
			SAXException, SCMException, IOException {
		final DefaultConfiguration dc = generalDao.getDefaultConfiguration();

		SCMConnectorConfig scmc = dc.getScm();

		SCMConnector<SCMConnectorConfig> scm = _scmManager
				.getConnectorInstance(scmc);

		System.out.println("Using SCM: " + scm.toString());
		File trunkDir = scm.createProjectDirectories(project, scmc);

		// Create Project from template.
		MavenCreateProject.createProjectWithMavenCore(project, trunkDir
				.getAbsolutePath());

		RMSConnector rms = _rmsManager.getConnectorInstance(dc.getRms());

		File pomXml = new File(trunkDir, project.getName() + "/pom.xml");

		// Set ScmUrl
		project.setScmUrl(scmc.getScmUrl() + "/" + project.getName()
				+ "/trunk/" + project.getName());

		// Inject SCM configuration
		try {
			MavenConfigureSCM.injectScm(pomXml, dc.getScm(), project);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		// Inject DistributionManagement configuration
		if (rms != null) {
			try {
				MavenConfigureDistributionManager.injectDistributionManager(
						pomXml, dc.getRms());
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}

		// Commit Project
		scm.commitProject(project, scmc);

		// Create a directory structure in subversion for the project
		// svn.createProject(project);

		// Add files in the trunk.

		// Add Project in Issue Tracking System
		ITSConnectorConfig config = dc.getIts();
		ITSConnector its = _itsManager.getConnectorInstance(config);
		if (its != null) {
			// its
			// .setUrl("http://localhost/cgi-bin/bugzilla/index.cgi?GoAheadAndLogIn=1");
			// its.login("", "");

			// its.setUrl("http://localhost/redmine");
			its.login("admin", "admin123", config);
			its.addProject(project, config);
			its.addModule(project, config, project.getName(), "initial module",
					"myemail@gmail.com");
			its.addVersion(project, config, "0.0.1");
		}

		CISConnector cis = (CISConnector) _itsManager.getConnectorInstance(dc
				.getCis());
		if (cis != null) {
			cis.addProject(project, dc.getCis());
		}
	}

}
