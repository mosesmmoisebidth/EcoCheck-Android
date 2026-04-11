package com.moses.inspectionapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Text
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.store.PhotoCaptureTarget
import com.moses.inspectionapp.ui.components.BottomNavBar
import com.moses.inspectionapp.ui.components.BottomNavItem
import com.moses.inspectionapp.ui.screens.home.HomeScreen
import com.moses.inspectionapp.ui.screens.inspections.InspectionsScreen
import com.moses.inspectionapp.ui.screens.inspections.InspectionDetailsScreen
import com.moses.inspectionapp.ui.screens.inspections.InspectionPdfScreen
import com.moses.inspectionapp.ui.screens.profile.ProfileScreen
import com.moses.inspectionapp.ui.screens.profile.NotificationHistoryScreen
import com.moses.inspectionapp.ui.screens.profile.SettingsScreen
import com.moses.inspectionapp.ui.screens.states.StatesDemoScreen
import com.moses.inspectionapp.ui.screens.sync.SyncScreen
import com.moses.inspectionapp.ui.screens.sync.SyncConflictsScreen
import com.moses.inspectionapp.ui.screens.stats.StatsScreen
import com.moses.inspectionapp.ui.screens.facilities.FacilityDetailsScreen
import com.moses.inspectionapp.ui.screens.facilities.FacilityEnrollScreen
import com.moses.inspectionapp.ui.screens.facilities.FacilitySearchScreen
import com.moses.inspectionapp.ui.screens.facilities.FacilitiesScreen
import com.moses.inspectionapp.ui.screens.camera.CameraCaptureScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentChecklistScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentCommentsScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentDecisionScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentAdjustmentScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentReviewScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentAnswersReviewScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentStartScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentTeamScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentVisitTypeScreen
import com.moses.inspectionapp.ui.screens.assessment.AssessmentQuestionsPreviewScreen
import com.moses.inspectionapp.ui.screens.edit.EditRecordScreen
import com.moses.inspectionapp.ui.screens.edit.LockedRecordScreen

@Composable
fun MainScaffold(modifier: Modifier = Modifier, onLogout: () -> Unit = {}) {
    val repository = AppContainer.repository
    val isOffline by repository.isOffline.collectAsState()
    val lastSyncLabel by repository.lastSyncLabel.collectAsState()
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem(MainRoute.Home, "Home", Icons.Rounded.Home),
        BottomNavItem(MainRoute.Facilities, "Facilities", Icons.Rounded.Storefront),
        BottomNavItem(MainRoute.Inspections, "Inspections", Icons.Rounded.Assignment),
        BottomNavItem(MainRoute.Sync, "Sync", Icons.Rounded.Sync),
        BottomNavItem(MainRoute.Profile, "Profile", Icons.Rounded.Person),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val assessmentStepRoutes = mapOf(
        1 to MainRoute.AssessmentStart,
        2 to MainRoute.AssessmentVisitType,
        3 to MainRoute.AssessmentTeam,
        4 to MainRoute.AssessmentAnswersReview,
        5 to MainRoute.AssessmentAdjustment,
        6 to MainRoute.AssessmentDecision,
        7 to MainRoute.AssessmentComments,
        8 to MainRoute.AssessmentReview,
    )
    val navigateToAssessmentStep: (Int) -> Unit = { step ->
        val route = assessmentStepRoutes[step]
        if (route != null && !navController.popBackStack(route, false)) {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavBar(
                items = items,
                currentRoute = currentRoute,
                onSelect = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 2 },
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 2 },
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 2 },
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 2 },
                ) + fadeOut(animationSpec = tween(300))
            },
        ) {
            composable(MainRoute.Home) {
                HomeScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNewAssessment = {
                        DraftStore.resetInspectionDraft()
                        navController.navigate(MainRoute.AssessmentStart)
                    },
                    onEnrollFacility = { navController.navigate(MainRoute.FacilityEnroll) },
                    onMyInspections = { navController.navigate(MainRoute.Inspections) },
                    onSyncNow = { navController.navigate(MainRoute.Sync) },
                    onStats = { navController.navigate(MainRoute.Stats) },
                )
            }
            composable(MainRoute.Facilities) {
                FacilitiesScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSelectFacility = { navController.navigate(MainRoute.FacilityDetails) },
                    onEnrollNew = { navController.navigate(MainRoute.FacilityEnroll) },
                )
            }
            composable(MainRoute.FacilitySearch) {
                FacilitySearchScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSelectFacility = { navController.navigate(MainRoute.FacilityDetails) },
                    onEnrollNew = { navController.navigate(MainRoute.FacilityEnroll) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.FacilityEnroll) {
                FacilityEnrollScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSaved = {
                        navController.navigate(MainRoute.Facilities) {
                            popUpTo(MainRoute.FacilityEnroll) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onCapturePhoto = {
                        DraftStore.photoCaptureTarget.value = PhotoCaptureTarget.FACILITY
                        navController.navigate(MainRoute.CameraCapture)
                    },
                )
            }
            composable(MainRoute.FacilityDetails) {
                FacilityDetailsScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onStartAssessment = { navController.navigate(MainRoute.AssessmentStart) },
                    onEdit = { navController.navigate(MainRoute.EditRecord) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.AssessmentStart) {
                AssessmentStartScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onContinue = { navController.navigate(MainRoute.AssessmentVisitType) },
                    onEnrollNew = { navController.navigate(MainRoute.FacilityEnroll) },
                    onSearch = { navController.navigate(MainRoute.FacilitySearch) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentVisitType) {
                AssessmentVisitTypeScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentTeam) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentTeam) {
                AssessmentTeamScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentQuestionsPreview) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentQuestionsPreview) {
                AssessmentQuestionsPreviewScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onStartQuestions = { navController.navigate(MainRoute.AssessmentChecklist) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentChecklist) {
                AssessmentChecklistScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentAnswersReview) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentAnswersReview) {
                AssessmentAnswersReviewScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentAdjustment) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentAdjustment) {
                AssessmentAdjustmentScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentDecision) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentDecision) {
                AssessmentDecisionScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentComments) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.AssessmentComments) {
                AssessmentCommentsScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onNext = { navController.navigate(MainRoute.AssessmentReview) },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                    onCapturePhoto = {
                        DraftStore.photoCaptureTarget.value = PhotoCaptureTarget.INSPECTION
                        navController.navigate(MainRoute.CameraCapture)
                    },
                )
            }
            composable(MainRoute.AssessmentReview) {
                AssessmentReviewScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSubmit = {
                        navController.navigate(MainRoute.Inspections) {
                            popUpTo(MainRoute.AssessmentStart) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onStepClick = navigateToAssessmentStep,
                )
            }
            composable(MainRoute.Inspections) {
                InspectionsScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSelectInspection = { navController.navigate(MainRoute.InspectionDetails) },
                    onNewAssessment = {
                        DraftStore.resetInspectionDraft()
                        navController.navigate(MainRoute.AssessmentStart)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.InspectionDetails) {
                InspectionDetailsScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onSharePdf = { navController.navigate(MainRoute.InspectionPdf) },
                    onEdit = { navController.navigate(MainRoute.EditRecord) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.InspectionPdf) {
                InspectionPdfScreen(
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.CameraCapture) {
                CameraCaptureScreen(
                    onCaptured = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.Sync) {
                SyncScreen(
                    isOffline = isOffline,
                    lastSyncLabel = lastSyncLabel,
                    onViewConflicts = { navController.navigate(MainRoute.SyncConflicts) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.SyncConflicts) {
                SyncConflictsScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoute.Stats) { StatsScreen(onBack = { navController.popBackStack() }) }
            composable(MainRoute.Settings) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNotificationHistory = { navController.navigate(MainRoute.Notifications) },
                )
            }
            composable(MainRoute.Notifications) {
                NotificationHistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoute.Profile) {
                ProfileScreen(
                    onSettings = { navController.navigate(MainRoute.Settings) },
                    onLogout = onLogout,
                )
            }
            composable(MainRoute.EditRecord) {
                EditRecordScreen(
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(MainRoute.LockedRecord) { LockedRecordScreen(onBack = { navController.popBackStack() }) }
            composable(MainRoute.StatesDemo) { StatesDemoScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
