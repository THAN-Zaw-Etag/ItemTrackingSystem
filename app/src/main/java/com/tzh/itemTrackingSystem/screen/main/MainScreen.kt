package com.tzh.itemTrackingSystem.screen.main


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.screen.addItem.AddItemViewModel
import com.tzh.itemTrackingSystem.screen.common.AppTitle
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.DeviceConnectionText
import com.tzh.itemTrackingSystem.screen.common.MyDropDown
import com.tzh.itemTrackingSystem.screen.common.RfidImage
import com.tzh.itemTrackingSystem.screen.dialog.BluetoothBottomSheet
import com.tzh.itemTrackingSystem.screen.dialog.ConnectingDialog
import com.tzh.itemTrackingSystem.screen.dialog.PowerBottomSheet
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ui.navigation.addScreenNavGraph
import com.tzh.itemTrackingSystem.ui.navigation.homeNavGraph
import com.tzh.itemTrackingSystem.ui.navigation.itemDetailNavGraph
import com.tzh.itemTrackingSystem.ui.navigation.planDetailNavGraph
import com.tzh.itemTrackingSystem.ui.navigation.planNavGraph
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import com.tzh.itemTrackingSystem.ulti.Constant
import com.tzh.itemTrackingSystem.ulti.Constant.ADD_CATEGORY
import com.tzh.itemTrackingSystem.ulti.Constant.ADD_ITEM
import com.tzh.itemTrackingSystem.ulti.Constant.ADD_PLAN
import com.tzh.itemTrackingSystem.ulti.Extensions.getScanStatus
import com.tzh.itemTrackingSystem.ulti.Extensions.getStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    bluetoothService: BluetoothService,
    application: ItemTrackingSystemApplication,
    mainViewModel: MainViewModel,
    itemViewModel: AddItemViewModel = viewModel(
        factory = AddItemViewModel.Companion.FACTORY(
            application.itemRepository, application.categoryRepository
        )
    ),
    showToast: (String) -> Unit,
) {
    val navController = rememberNavController()
    val deviceList by mainViewModel.btDeviceList.collectAsState()
    val uiState by mainViewModel.uiState.collectAsState()
    ConnectingDialog(isShow = uiState.selectedBtDevice != null, uiState.selectedBtDevice)
//    StartConnecting(
//        uiState.selectedBtDevice,
//        bluetoothService,
//        mainViewModel,
//        uiState.connectionStatus,
//        showToast,
//    ) {
//        mainViewModel.setSelectedDevice(null)
//    }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    BluetoothBottomSheet(
        isShow = uiState.showBtSheet,
        sheetState = modalBottomSheetState,
        bluetoothService = bluetoothService,
        deviceList = deviceList,
        dismiss = mainViewModel::dismissBtSheet,
        onConnect = mainViewModel::setSelectedDevice,
    )
    PowerBottomSheet(
        isShow = uiState.showPowerSheet,
        sheetState = modalBottomSheetState,
        dismiss = mainViewModel::dismissPowerSheet,
        onSave = {
            if (!uiState.isScanning) {
                val isSuccess = bluetoothService.setPower(it)
                showToast(
                    if (isSuccess) {
                        "Successfully set power $it"
                    } else {
                        "Fail to set power"
                    }
                )
                mainViewModel.dismissPowerSheet()
            } else {
                showToast("Reader is running.")
            }
        },
    )
    ControlBluetoothLifecycle(
        LocalLifecycleOwner.current,
        onCreate = {
            bluetoothService.setOnDiscoverListener(mainViewModel)
        },
        onResume = {
            bluetoothService.setScanStateListener(mainViewModel)
        },
        onPause = {
            mainViewModel.stopScan(bluetoothService)
            bluetoothService.removeScanStateListener(mainViewModel)
        },
        onDestroy = {
            bluetoothService.removeOnDiscoverListener()
        },
    )
    val expended = remember {
        mutableStateOf(false)
    }
    MyDropDown(
        expanded = expended, list = Constant.addDropDownMenu,
        onItemClick = {
            expended.value = false
            when (it) {
                ADD_ITEM -> {
                    itemViewModel.resetData()
                    navController.navigate(ROUTE.AddItem)
                }

                ADD_CATEGORY -> {
                    navController.navigate(ROUTE.AddCategory)
                }

                ADD_PLAN -> {
                    navController.navigate(ROUTE.AddPlan)
                }
            }
        },
    )
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.currentRoute == ROUTE.Home) {
                                expended.value = true
                            }
                        },
                    ) {
                        Image(painter = painterResource(id = R.drawable.add), contentDescription = "Setting")
                    }
                    IconButton(
                        onClick = { mainViewModel.showBtSheet() },
                    ) {
                        Image(painter = painterResource(id = R.drawable.bluetooth), contentDescription = "Bluetooth")
                    }
                    IconButton(
                        onClick = { mainViewModel.showPowerSheet() },
                        enabled = uiState.connectionStatus == ConnectionStatus.CONNECTED
                    ) {
                        Image(painter = painterResource(id = R.drawable.setting), contentDescription = "Setting")
                    }
                    IconButton(onClick = {
                        if (uiState.currentRoute == ROUTE.Home) {
                            navController.navigate(ROUTE.PlanScreen)
                        }

                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.plan),
                            contentDescription = "Setting",
                            modifier = Modifier.padding(4.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    IconButton(onClick = {
                        if (uiState.currentRoute != ROUTE.Home) {
                            navController.navigate(ROUTE.Home) {
                                popUpTo(ROUTE.Home) {
                                    inclusive = true
                                }
                            }
                        }
                    }) {
                        Image(
                            modifier = Modifier.padding(4.dp),
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home"
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            mainViewModel.toggleScan(bluetoothService, showToast)
                        },
                    ) {
                        RfidImage()
                    }
                },
            )
        },
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(8.dp), color = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTitle()
                DeviceConnectionText(
                    getStatus(uiState.connectionStatus, uiState.connectedDeviceName), modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = uiState.connectionStatus == ConnectionStatus.CONNECTED) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        DeviceConnectionText(getScanStatus(uiState.isScanning))
                        Spacer(modifier = Modifier.width(16.dp))
                        AnimatedVisibility(visible = uiState.isScanning) {
                            CircularProgressIndicator()
                        }
                    }
                }
                NavHost(
                    modifier = Modifier.weight(1f), navController = navController, startDestination = ROUTE.Home
                ) {
                    homeNavGraph(
                        uiState.currentRoute,
                        navController = navController,
                        itemRepository = application.itemRepository,
                        addItemViewModel = itemViewModel,
                        bluetoothService = bluetoothService,
                        setCurrentRoute = mainViewModel::setCurrentRoute
                    )

                    addScreenNavGraph(
                        bluetoothService,
                        itemViewModel,
                        navController,
                        application,
                        mainViewModel::setCurrentRoute,
                    )

                    itemDetailNavGraph(
                        bluetoothService,
                        application.itemRepository,
                        mainViewModel::setCurrentRoute,
                    )



                    planNavGraph(
                        navController = navController,
                        application.planRepository,
                        mainViewModel::setCurrentRoute,
                    )

                    planDetailNavGraph(
                        bluetoothService, application, mainViewModel::setCurrentRoute
                    )
                }
            }
        }
    }
}
