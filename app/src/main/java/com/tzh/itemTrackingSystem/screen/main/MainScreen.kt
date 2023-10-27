package com.tzh.itemTrackingSystem.screen.main


import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.dialog.StartConnecting
import com.tzh.itemTrackingSystem.screen.addItem.AddItemScreen
import com.tzh.itemTrackingSystem.screen.addItem.AddItemViewModel
import com.tzh.itemTrackingSystem.screen.common.AppTitle
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.DeviceConnectionText
import com.tzh.itemTrackingSystem.screen.common.MyDropDown
import com.tzh.itemTrackingSystem.screen.common.RfidImage
import com.tzh.itemTrackingSystem.screen.createCategory.CreateCategoryScreen
import com.tzh.itemTrackingSystem.screen.dialog.BluetoothBottomSheet
import com.tzh.itemTrackingSystem.screen.dialog.ConnectingDialog
import com.tzh.itemTrackingSystem.screen.dialog.PowerBottomSheet
import com.tzh.itemTrackingSystem.screen.item.ItemScreen
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.service.BluetoothService
import com.tzh.itemTrackingSystem.ulti.ConnectionStatus
import com.tzh.itemTrackingSystem.ulti.Constant
import com.tzh.itemTrackingSystem.ulti.Constant.ADD_CATEGORY
import com.tzh.itemTrackingSystem.ulti.Constant.ADD_ITEM
import com.tzh.itemTrackingSystem.ulti.Extensions.getScanStatus
import com.tzh.itemTrackingSystem.ulti.Extensions.getStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    bluetoothService: BluetoothService,
    application: ItemTrackingSystemApplication,
    showToast: (String) -> Unit,
    itemViewModel: AddItemViewModel = viewModel(
        factory = AddItemViewModel.Companion.FACTORY(
            application.itemRepository, application.categoryRepository
        )
    )
) {

    val navController = rememberNavController()

    val deviceList by mainViewModel.btDeviceList.collectAsState()
    val isScanning by mainViewModel.isScanning.collectAsState()
    val connectionState by mainViewModel.connectionStatus.collectAsState()
    val deviceName by mainViewModel.connectedDeviceName.collectAsState()

    var selectedBtDevice: BluetoothDevice? by remember { mutableStateOf(null) }

    ConnectingDialog(isShow = selectedBtDevice != null, selectedBtDevice)
    StartConnecting(selectedBtDevice, bluetoothService, mainViewModel, showToast) {
        selectedBtDevice = null
    }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bluetoothShowSheet by rememberSaveable { mutableStateOf(false) }
    var powerShowSheet by rememberSaveable {
        mutableStateOf(false)
    }
    BluetoothBottomSheet(
        isShow = bluetoothShowSheet,
        sheetState = modalBottomSheetState,
        bluetoothService = bluetoothService,
        deviceList = deviceList,
        dismiss = {
            bluetoothShowSheet = false
        },
        onConnect = {
            bluetoothShowSheet = false
            selectedBtDevice = it
        },
    )
    PowerBottomSheet(
        isShow = powerShowSheet, sheetState = modalBottomSheetState,
        dismiss = {
            powerShowSheet = false
        },
        onSave = {
            val isSuccess = bluetoothService.setPower(it)
            showToast(
                if (isSuccess) {
                    "Successfully set power $it"
                } else {
                    "Fail to set power"
                }
            )
            powerShowSheet = false
        },
    )
    ControlBluetoothLifecycle(LocalLifecycleOwner.current, onCreate = {
        bluetoothService.setOnDiscoverListener(mainViewModel)
    }, onPause = {

    }, onDestroy = {
        mainViewModel.stopScan(bluetoothService)
        bluetoothService.removeOnDiscoverListener()
    })

    var currentRoute by remember {
        mutableStateOf("")
    }

//    LaunchedEffect(key1 = currentRoute) {
//        if (currentRoute.isNotEmpty()) {
//            currentRoute = ""
//        }
//    }

    val expended = remember {
        mutableStateOf(false)
    }
    MyDropDown(expanded = expended, list = Constant.addDropDownMenu, onItemClick = {
        expended.value = false
        if (it == ADD_ITEM) {
            itemViewModel.resetData()
            navController.navigate(ROUTE.AddItem)
        } else if (it == ADD_CATEGORY) {
            navController.navigate(ROUTE.AddCategory)
        }

    })
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            if (currentRoute == ROUTE.Home) {
                                expended.value = true
                            }
                        },
                    ) {
                        Image(painter = painterResource(id = R.drawable.add), contentDescription = "Setting")
                    }
                    IconButton(
                        onClick = { bluetoothShowSheet = true },
                    ) {
                        Image(painter = painterResource(id = R.drawable.bluetooth), contentDescription = "Bluetooth")
                    }
                    IconButton(onClick = { powerShowSheet = true }, enabled = connectionState == ConnectionStatus.CONNECTED) {
                        Image(painter = painterResource(id = R.drawable.setting), contentDescription = "Setting")
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
                DeviceConnectionText(getStatus(connectionState, deviceName), modifier = Modifier.fillMaxWidth())
                if (connectionState == ConnectionStatus.CONNECTED) {
                    DeviceConnectionText(getScanStatus(isScanning), modifier = Modifier.fillMaxWidth())
                }
                NavHost(
                    modifier = Modifier.weight(1f), navController = navController, startDestination = ROUTE.Home
                ) {
                    composable(ROUTE.Home) {
                        Log.e("ROUTE is ", ROUTE.Home)
                        currentRoute = ROUTE.Home
                        ItemScreen(bluetoothService = bluetoothService,
                            modifier = Modifier.fillMaxSize(),
                            itemRepository = application.itemRepository,
                            editItem = { item ->
                                if (currentRoute == ROUTE.Home) {
                                    itemViewModel.setEditItem(item)
                                    navController.navigate(ROUTE.AddItem)
                                }
                            })
                    }
                    composable(ROUTE.AddItem) {
                        currentRoute = ROUTE.AddItem
                        Log.e("ROUTE is ", ROUTE.AddItem)
                        AddItemScreen(application, bluetoothService, itemViewModel, navController = navController)
                    }

                    composable(ROUTE.AddCategory) {
                        currentRoute = ROUTE.AddCategory
                        Log.e("ROUTE is ", ROUTE.AddItem)
                        CreateCategoryScreen { category ->
                            itemViewModel.addCategory(category) {
                                navController.navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }
}
