package com.lunserv.compolaunch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
    @Override
    public void launch( ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
        @SuppressWarnings( { "unchecked" } )
        final List< String > configurationNameList = configuration.getAttribute( CompositeLaunchTab.SELECTED_CONFIGURATION_LIST,
                Collections.EMPTY_LIST );

        final ILaunchManager launchManager = DebugPlugin.getDefault( ).getLaunchManager( );
        final ILaunchConfiguration[ ] allLaunchConfigurations = launchManager.getLaunchConfigurations( );
        final HashMap< String, ILaunchConfiguration > launchConfigurations = new HashMap<>( );
        for ( ILaunchConfiguration c : allLaunchConfigurations ) {
            launchConfigurations.put( c.getName( ), c );
        }

        for ( String name : configurationNameList ) {
            if ( launchConfigurations.containsKey( name ) ) {
                launchConfigurations.get( name ).launch( mode, null );
            }
        }
    }
}
