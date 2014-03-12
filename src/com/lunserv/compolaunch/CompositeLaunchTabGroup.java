package com.lunserv.compolaunch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CompositeLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {
    @Override
    public void createTabs( ILaunchConfigurationDialog dialog, String mode ) {
        ILaunchConfigurationTab[ ] tabs = { new CompositeLaunchTab( ), new CommonTab( ) };
        setTabs( tabs );
    }
}
