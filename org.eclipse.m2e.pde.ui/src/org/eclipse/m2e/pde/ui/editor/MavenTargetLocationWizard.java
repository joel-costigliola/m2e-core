/*******************************************************************************
 * Copyright (c) 2018, 2020 Christoph Läubrich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.m2e.pde.ui.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.pde.MavenTargetLocation;
import org.eclipse.m2e.pde.MissingMetadataMode;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.ui.target.ITargetLocationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MavenTargetLocationWizard extends Wizard implements ITargetLocationWizard {

	private Text artifactId;
	private Text groupId;
	private Text version;
	private CCombo type;
	private MavenTargetLocation targetLocation;
	private CCombo scope;
	private ComboViewer metadata;
	private ITargetDefinition targetDefinition;

	public MavenTargetLocationWizard() {
		this(null);
	}

	public MavenTargetLocationWizard(MavenTargetLocation targetLocation) {
		this.targetLocation = targetLocation;
		setWindowTitle("Maven Artifact Target Entry");
		WizardPage page = new WizardPage(
				targetLocation == null ? "Add a new Maven dependency" : "Edit Maven Dependency") {

			@Override
			public void createControl(Composite parent) {
				Composite composite = new Composite(parent, SWT.NONE);
				setControl(composite);
				composite.setLayout(new GridLayout(2, false));
				new Label(composite, SWT.NONE).setText("Group Id");
				groupId = fill(new Text(composite, SWT.BORDER));
				new Label(composite, SWT.NONE).setText("Artifact Id");
				artifactId = fill(new Text(composite, SWT.BORDER));
				new Label(composite, SWT.NONE).setText("Version");
				version = fill(new Text(composite, SWT.BORDER));
				new Label(composite, SWT.NONE).setText("Type");
				type = new CCombo(composite, SWT.BORDER);
				type.add("jar");
				type.add("bundle");
				new Label(composite, SWT.NONE).setText("Missing OSGi-Manifest");
				metadata = new ComboViewer(composite);
				metadata.setContentProvider(ArrayContentProvider.getInstance());
				metadata.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof MissingMetadataMode) {
							return ((MissingMetadataMode) element).name().toLowerCase();
						}
						return super.getText(element);
					}

				});
				metadata.setInput(MissingMetadataMode.values());
				new Label(composite, SWT.NONE).setText("Dependencies scope");
				scope = new CCombo(composite, SWT.BORDER);
				scope.add("");
				scope.add("compile");
				scope.add("test");
				scope.add("provided");
				if (targetLocation != null) {
					artifactId.setText(targetLocation.getArtifactId());
					groupId.setText(targetLocation.getGroupId());
					version.setText(targetLocation.getVersion());
					type.setText(targetLocation.getArtifactType());
					scope.setText(targetLocation.getDependencyScope());
					metadata.setSelection(new StructuredSelection(targetLocation.getMetadataMode()));
				} else {
					artifactId.setText("");
					groupId.setText("");
					version.setText("");
					type.setText(MavenTargetLocation.DEFAULT_PACKAGE_TYPE);
					scope.setText(MavenTargetLocation.DEFAULT_DEPENDENCY_SCOPE);
					metadata.setSelection(new StructuredSelection(MavenTargetLocation.DEFAULT_METADATA_MODE));
				}
			}

			private Text fill(Text text) {
				GridData data = new GridData(GridData.FILL_HORIZONTAL);
				data.grabExcessHorizontalSpace = true;
				text.setLayoutData(data);
				return text;
			}
		};
		page.setImageDescriptor(ImageDescriptor.createFromURL(
				MavenTargetLocationWizard.class.getResource("/icons/new_m2_project_wizard.gif")));
		page.setTitle(page.getName());
		page.setDescription("Enter the desired maven artifact to add to the target platform");
		addPage(page);

	}

	@Override
	public void setTarget(ITargetDefinition target) {
		this.targetDefinition = target;
	}

	@Override
	public ITargetLocation[] getLocations() {
		return new ITargetLocation[] { targetLocation };
	}

	@Override
	public boolean performFinish() {
		if (targetLocation == null) {
			targetLocation = new MavenTargetLocation(groupId.getText(), artifactId.getText(), version.getText(),
					type.getText(), (MissingMetadataMode) metadata.getStructuredSelection().getFirstElement(),
					scope.getText());
		} else {
			ITargetLocation[] locations = targetDefinition.getTargetLocations().clone();
			for (int i = 0; i < locations.length; i++) {
				if (locations[i] == targetLocation) {
					locations[i] = new MavenTargetLocation(groupId.getText(), artifactId.getText(), version.getText(),
							type.getText(), (MissingMetadataMode) metadata.getStructuredSelection().getFirstElement(),
							scope.getText());
				}

			}
			targetDefinition.setTargetLocations(locations);
		}
		return true;
	}

}
