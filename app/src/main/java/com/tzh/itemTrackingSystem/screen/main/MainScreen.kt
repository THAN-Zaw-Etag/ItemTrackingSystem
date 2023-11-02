package com.tzh.itemTrackingSystem.screen.main


import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tzh.itemTrackingSystem.ItemTrackingSystemApplication
import com.tzh.itemTrackingSystem.R
import com.tzh.itemTrackingSystem.screen.addItem.AddItemScreen
import com.tzh.itemTrackingSystem.screen.addItem.AddItemViewModel
import com.tzh.itemTrackingSystem.screen.addPlan.CreatePlanScreen
import com.tzh.itemTrackingSystem.screen.common.AppTitle
import com.tzh.itemTrackingSystem.screen.common.ControlBluetoothLifecycle
import com.tzh.itemTrackingSystem.screen.common.DeviceConnectionText
import com.tzh.itemTrackingSystem.screen.common.MyDropDown
import com.tzh.itemTrackingSystem.screen.common.RfidImage
import com.tzh.itemTrackingSystem.screen.createCategory.CreateCategoryScreen
import com.tzh.itemTrackingSystem.screen.detail_Item.DetailItemScreen
import com.tzh.itemTrackingSystem.screen.dialog.BluetoothBottomSheet
import com.tzh.itemTrackingSystem.screen.dialog.ConnectingDialog
import com.tzh.itemTrackingSystem.screen.dialog.PowerBottomSheet
import com.tzh.itemTrackingSystem.screen.dialog.StartConnecting
import com.tzh.itemTrackingSystem.screen.item.ItemScreen
import com.tzh.itemTrackingSystem.screen.navigation.ROUTE
import com.tzh.itemTrackingSystem.screen.plan.PlanScreen
import com.tzh.itemTrackingSystem.screen.plan.planDetail.PlanDetailScreen
import com.tzh.itemTrackingSystem.service.BluetoothService
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
    mainViewModel: MainViewModel,
    bluetoothService: BluetoothService,
    application: ItemTrackingSystemApplication,
    showToast: (String) -> Unit,
    previousBTAddress: String?,
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

    var selectedBtDevice: String? by remember { mutableStateOf(previousBTAddress) }
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
            if (!isScanning) {
                val isSuccess = bluetoothService.setPower(it)
                showToast(
                    if (isSuccess) {
                        "Successfully set power $it"
                    } else {
                        "Fail to set power"
                    }
                )
                powerShowSheet = false
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
                    IconButton(onClick = {
                        if (currentRoute == ROUTE.Home) {
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
                        if (currentRoute != ROUTE.Home) {
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
                DeviceConnectionText(getStatus(connectionState, deviceName), modifier = Modifier.fillMaxWidth())
                if (connectionState == ConnectionStatus.CONNECTED) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        DeviceConnectionText(getScanStatus(isScanning))
                        Spacer(modifier = Modifier.width(16.dp))
                        AnimatedVisibility(visible = isScanning) {
                            CircularProgressIndicator()
                        }
                    }
                }
                NavHost(
                    modifier = Modifier.weight(1f), navController = navController, startDestination = ROUTE.Home
                ) {
                    composable(ROUTE.Home) {
                        Log.e("ROUTE is ", ROUTE.Home)
                        currentRoute = ROUTE.Home
                        ItemScreen(
                            bluetoothService = bluetoothService,
                            modifier = Modifier.fillMaxSize(),
                            itemRepository = application.itemRepository,
                            editItem = { item ->
                                if (currentRoute == ROUTE.Home) {
                                    itemViewModel.setEditItem(item)
                                    navController.navigate(ROUTE.AddItem)
                                }
                            },
                            onClick = { item ->
                                if (currentRoute == ROUTE.Home) {
                                    navController.navigate(ROUTE.DetailScreen + "/${item.id}")
                                }
                            },
                        )
                    }

                    composable(ROUTE.AddItem) {
                        currentRoute = ROUTE.AddItem
                        Log.e("ROUTE is ", ROUTE.AddItem)
                        AddItemScreen(bluetoothService, itemViewModel, navController = navController)
                    }

                    composable(ROUTE.AddCategory) {
                        currentRoute = ROUTE.AddCategory
                        Log.e("ROUTE is ", ROUTE.AddCategory)
                        CreateCategoryScreen { category ->
                            itemViewModel.addCategory(category) {
                                navController.navigateUp()
                            }
                        }
                    }

                    composable(ROUTE.AddPlan) {
                        currentRoute = ROUTE.AddPlan
                        Log.e("ROUTE is ", ROUTE.AddPlan)
                        CreatePlanScreen(navController = navController, planRepository = application.planRepository)
                    }

                    composable(
                        ROUTE.DetailScreen + "/{id}", arguments = listOf(
                            navArgument("id") {
                                type = NavType.IntType
                                defaultValue = 0
                            },
                        )
                    ) { backStackEntry ->
                        currentRoute = ROUTE.DetailScreen
                        Log.e("ROUTE is ", ROUTE.DetailScreen)
                        val itemId = backStackEntry.arguments?.getInt("id") ?: 0
                        DetailItemScreen(itemId = itemId, itemRepository = application.itemRepository, bluetoothService)
                    }

                    composable(ROUTE.PlanScreen) {
                        currentRoute = ROUTE.PlanScreen
                        Log.e("ROUTE is ", ROUTE.PlanScreen)
                        PlanScreen(navController, planRepository = application.planRepository)
                    }

                    composable(
                        ROUTE.PlanDetailScreen + "/{planId}/{planName}", arguments = listOf(
                            navArgument("planId") {
                                type = NavType.IntType
                                defaultValue = 0
                            },
                            navArgument("planName") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        )
                    ) { backStackEntry ->
                        currentRoute = ROUTE.PlanDetailScreen
                        Log.e("ROUTE is ", ROUTE.PlanDetailScreen)
                        val planId = backStackEntry.arguments?.getInt("planId") ?: 0
                        val planName = backStackEntry.arguments?.getString("planName") ?: ""
                        PlanDetailScreen(
                            bluetoothService,
                            planId,
                            planName,
                            planRepository = application.planRepository,
                            itemRepository = application.itemRepository
                        )
                    }
                }
            }
        }
    }
}
