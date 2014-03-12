package com.lunserv.compolaunch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CompositeLaunchTab extends AbstractLaunchConfigurationTab {
    public static final String SELECTED_CONFIGURATION_LIST = "selectedConfigurationList";
    private Button removeConfigs;
    private final ArrayList< String > allConfigNames = new ArrayList<>( );

    private TableViewer availableConfigs;
    private final IObservableList availableConfigsData = new WritableList( new ArrayList<>( ), String.class );

    private TableViewer selectedConfigs;
    private final IObservableList selectedConfigsData = new WritableList( new ArrayList<>( ), String.class );

    @Override
    public void createControl( Composite parent ) {
        final Composite controls = new Composite( parent, SWT.FILL );
        controls.setLayout( new FormLayout( ) );

        final Label avConfLbl = new Label( controls, SWT.LEFT );
        avConfLbl.setText( "Available launch configurations: " );
        final FormData avConfLblF = new FormData( );
        avConfLblF.left = new FormAttachment( 0, 5 );
        avConfLblF.right = new FormAttachment( 50, -5 );
        avConfLblF.top = new FormAttachment( 0, 5 );
        avConfLbl.setLayoutData( avConfLblF );

        availableConfigs = new TableViewer( controls, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE );
        availableConfigs.setContentProvider( new ObservableListContentProvider( ) );
        availableConfigs.setInput( availableConfigsData );

        final FormData availableConfigsF = new FormData( );
        availableConfigsF.left = new FormAttachment( 0, 5 );
        availableConfigsF.right = new FormAttachment( 50, -5 );
        availableConfigsF.top = new FormAttachment( avConfLbl, 5 );
        availableConfigsF.bottom = new FormAttachment( 100, -5 );
        availableConfigs.getTable( ).setLayoutData( availableConfigsF );

        final Label selConfLbl = new Label( controls, SWT.LEFT );
        selConfLbl.setText( "Selected launch configurations:" );
        final FormData selConfLblF = new FormData( );
        selConfLblF.left = new FormAttachment( 50, 5 );
        selConfLblF.right = new FormAttachment( 100, -5 );
        selConfLblF.top = new FormAttachment( 0, 5 );
        selConfLbl.setLayoutData( selConfLblF );

        removeConfigs = new Button( controls, SWT.PUSH );
        removeConfigs.setText( "Remove" );
        removeConfigs.setEnabled( false );
        final FormData removeCnfigsF = new FormData( );
        removeCnfigsF.left = new FormAttachment( 50, 5 );
        removeCnfigsF.right = new FormAttachment( 100, -5 );
        removeCnfigsF.bottom = new FormAttachment( 100, -5 );
        removeConfigs.setLayoutData( removeCnfigsF );

        selectedConfigs = new TableViewer( controls, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION );
        selectedConfigs.setContentProvider( new ObservableListContentProvider( ) );
        selectedConfigs.setInput( selectedConfigsData );

        final FormData selectedConfigsListF = new FormData( );
        selectedConfigsListF.left = new FormAttachment( 50, 5 );
        selectedConfigsListF.right = new FormAttachment( 100, -5 );
        selectedConfigsListF.top = new FormAttachment( selConfLbl, 5 );
        selectedConfigsListF.bottom = new FormAttachment( removeConfigs, -5 );
        selectedConfigs.getTable( ).setLayoutData( selectedConfigsListF );

        setLauncIcons( controls );
        addListeners( );
        setControl( controls );
    }

    private void setLauncIcons( Composite controls ) {
        final ILaunchManager launchManager = getLaunchManager( );
        ILaunchConfiguration[ ] launchConfigurations;

        final HashMap< String, Image > imgs = new HashMap<>( );

        try {
            launchConfigurations = launchManager.getLaunchConfigurations( );
        }
        catch ( CoreException e ) {
            setErrorMessage( e.getMessage( ) );
            return;
        }

        final IDebugModelPresentation debugModelPresentation = DebugUITools.newDebugModelPresentation( );

        for ( ILaunchConfiguration conf : launchConfigurations ) {
            final Image image = debugModelPresentation.getImage( conf );
            imgs.put( conf.getName( ), image );
        }

        availableConfigs.setLabelProvider( new LabelProvider( ) {
            @Override
            public Image getImage( Object element ) {
                return imgs.get( element.toString( ) );
            }
        } );

        selectedConfigs.setLabelProvider( new LabelProvider( ) {
            @Override
            public Image getImage( Object element ) {
                return imgs.get( element.toString( ) );
            }
        } );
    }

    private void addListeners( ) {
        availableConfigs.addSelectionChangedListener( new ISelectionChangedListener( ) {
            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                addAction( );
            }
        } );

        selectedConfigs.addSelectionChangedListener( new ISelectionChangedListener( ) {
            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                updateLaunchConfigurationState( );
            }
        } );

        removeConfigs.addListener( SWT.Selection, new Listener( ) {
            @Override
            public void handleEvent( Event event ) {
                removeAction( );
            }
        } );

        final Transfer[ ] transferTypes = new Transfer[ ] { LocalSelectionTransfer.getTransfer( ) };
        selectedConfigs.addDragSupport( DND.DROP_MOVE, transferTypes, new DragSourceListener( ) {
            @Override
            public void dragStart( DragSourceEvent event ) {
                final IStructuredSelection sel = ( IStructuredSelection ) selectedConfigs.getSelection( );
                LocalSelectionTransfer.getTransfer( ).setSelection( sel );
                event.doit = true;
            }

            @Override
            public void dragSetData( DragSourceEvent event ) {
                event.data = LocalSelectionTransfer.getTransfer( ).getSelection( );
            }

            @Override
            public void dragFinished( DragSourceEvent event ) {
                LocalSelectionTransfer.getTransfer( ).setSelection( null );
            }
        } );

        selectedConfigs.addDropSupport( DND.DROP_MOVE, transferTypes, new ViewerDropAdapter( selectedConfigs ) {
            String insTarget;
            int insType;

            @Override
            public void drop( DropTargetEvent event ) {
                insType = this.determineLocation( event );
                insTarget = ( String ) determineTarget( event );

                super.drop( event );
            }

            @Override
            public boolean validateDrop( Object target, int operation, TransferData transferType ) {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public boolean performDrop( Object data ) {
                final IStructuredSelection sel = ( IStructuredSelection ) data;

                @SuppressWarnings( "rawtypes" )
                List sels = sel.toList( );

                for ( Object s : sels ) {
                    selectedConfigsData.remove( s );
                }

                int insIdex = selectedConfigsData.size( );
                if ( insType == 1 || insType == 3 ) {
                    insIdex = selectedConfigsData.indexOf( insTarget );
                }

                if ( insType == 2 ) {
                    insIdex = selectedConfigsData.indexOf( insTarget ) + 1;
                }

                selectedConfigsData.addAll( insIdex, sels );

                updateLaunchConfigurationState( );

                return false;
            }
        } );
    }

    private void updateLaunchConfigurationState( ) {
        removeConfigs.setEnabled( !selectedConfigs.getSelection( ).isEmpty( ) );

        updateLaunchConfigurationDialog( );
    }

    private void removeAction( ) {
        final IStructuredSelection sel = ( IStructuredSelection ) selectedConfigs.getSelection( );

        @SuppressWarnings( { "rawtypes" } )
        List sels = sel.toList( );

        selectedConfigsData.removeAll( sels );

        availableConfigsData.clear( );
        for ( String c : allConfigNames ) {
            if ( !selectedConfigsData.contains( c ) ) {
                availableConfigsData.add( c );
            }
        }

        updateLaunchConfigurationState( );
    }

    private void addAction( ) {
        final IStructuredSelection sel = ( IStructuredSelection ) availableConfigs.getSelection( );

        @SuppressWarnings( { "rawtypes" } )
        List sels = sel.toList( );

        selectedConfigsData.addAll( sels );
        availableConfigsData.removeAll( sels );

        updateLaunchConfigurationState( );
    }

    @Override
    @SuppressWarnings( { "unchecked" } )
    public void initializeFrom( ILaunchConfiguration configuration ) {
        final ILaunchManager launchManager = getLaunchManager( );
        ILaunchConfiguration[ ] launchConfigurations;

        try {
            launchConfigurations = launchManager.getLaunchConfigurations( );
        }
        catch ( CoreException e ) {
            setErrorMessage( e.getMessage( ) );
            return;
        }

        List< String > selectedList = new ArrayList<>( );
        try {
            selectedList = configuration.getAttribute( SELECTED_CONFIGURATION_LIST, Collections.EMPTY_LIST );
        }
        catch ( CoreException e ) {
            setErrorMessage( e.getMessage( ) );
        }

        allConfigNames.clear( );
        availableConfigsData.clear( );
        for ( ILaunchConfiguration conf : launchConfigurations ) {
            final String configurationName = conf.getName( );

            if ( !configurationName.equals( configuration.getName( ) ) ) {
                allConfigNames.add( configurationName );
                
                if ( !selectedList.contains( configurationName ) ) {
                    availableConfigsData.add( configurationName );
                }
            }
        }

        selectedConfigsData.clear( );
        for ( String s : selectedList ) {
            selectedConfigsData.add( s );

            if ( !allConfigNames.contains( s ) ) {
                setErrorMessage( "Configuration [" + s + "] is undefined" );
            }
        }

        updateLaunchConfigurationState( );
    }

    @Override
    public boolean isValid( ILaunchConfiguration launchConfig ) {
        for ( Object s : selectedConfigsData ) {
            if ( !allConfigNames.contains( s ) ) {
                setErrorMessage( "Configuration [" + s + "] is undefined" );
                return false;
            }
        }

        setErrorMessage( null );

        return super.isValid( launchConfig );
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
        configuration.setAttribute( SELECTED_CONFIGURATION_LIST, selectedConfigsData );
    }

    @Override
    public String getName( ) {
        return "Composite";
    }

    @Override
    public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
        // TODO Auto-generated method stub

    }
}
