package com.example.ui

import android.app.Application
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.data.BatchEntity
import com.example.data.LeadEntity
import com.example.data.StudentEntity
import com.example.data.StaffEntity
import com.example.data.FeeHistoryEntity
import com.example.data.AttendanceRecordEntity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.LocalAuthManager
import com.example.data.CloudAuthManager
import com.example.data.SupabaseConfig
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.util.WhatsAppTemplates
import com.example.ui.util.pressAnimation
import com.example.ui.util.ReceiptGenerator
import com.example.ui.util.ProfilePhotoPicker
import com.example.ui.util.ProfilePhotoManager
import com.example.ui.util.ValidationUtils as V
import com.example.data.DataExportImportManager
import com.example.data.TuitionRepository
import com.example.data.SettingsEntity
import io.github.jan.supabase.auth.auth
import com.example.data.supabaseClient
import com.example.ui.components.StatusBadge
import com.example.ui.components.BilingualLabel
import com.example.ui.components.AppLogo

// Screen Destination Constants
object Dest {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val STUDENT_LIST = "student_list"
    const val STUDENT_PROFILE = "student_profile/{studentId}"
    const val CREATE_BATCH = "create_batch"
    const val BATCH_ATTENDANCE = "batch_attendance/{batchId}"
    const val STAFF_DIRECTORY = "staff_directory"
    const val SETTINGS = "settings"
    const val LEADS = "leads"
    const val FEES_SCHEDULE = "fees_schedule"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TuitionOSApp() {
    val navController = rememberNavController()
    val viewModel: TuitionViewModel = viewModel()
    val context = LocalContext.current

    // Observe current route to conditionally show AI Chat Assistant
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Dest.SPLASH,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(200)) }
        ) {
            composable(Dest.SPLASH) {
                SplashScreen(navController)
            }
            composable(Dest.ONBOARDING) {
                OnboardingScreen(navController)
            }
            composable(Dest.LOGIN) {
                LoginScreen(navController, viewModel)
            }
            composable(Dest.DASHBOARD) {
                DashboardScreen(navController, viewModel)
            }
            composable(Dest.STUDENT_LIST) {
                StudentListScreen(navController, viewModel)
            }
            composable(
                route = Dest.STUDENT_PROFILE,
                arguments = listOf(navArgument("studentId") { type = NavType.IntType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
                StudentProfileScreen(navController, viewModel, studentId)
            }
            composable(Dest.CREATE_BATCH) {
                CreateBatchScreen(navController, viewModel)
            }
            composable(
                route = Dest.BATCH_ATTENDANCE,
                arguments = listOf(navArgument("batchId") { type = NavType.IntType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getInt("batchId") ?: 0
                BatchAttendanceScreen(navController, viewModel, batchId)
            }
            composable(Dest.STAFF_DIRECTORY) {
                StaffDirectoryScreen(navController, viewModel)
            }
            composable(Dest.SETTINGS) {
                SettingsScreen(navController, viewModel)
            }
            composable(Dest.FEES_SCHEDULE) {
                FeesScheduleScreen(navController, viewModel)
            }
            composable(Dest.LEADS) {
                LeadsScreen(navController, viewModel)
            }
        }

        // Display Floating AI Assistant only when logged in / active screens are shown
        val showAI = currentRoute != null && 
                     currentRoute != Dest.SPLASH && 
                     currentRoute != Dest.ONBOARDING && 
                     currentRoute != Dest.LOGIN

        if (showAI) {
            FloatingAiChat(
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val localAuth = remember { LocalAuthManager(context) }

    // Pulsing logo animation
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Fade-in entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "splashFade"
    )

    LaunchedEffect(Unit) {
        delay(2500)
        var isLoggedIn = false

        // Check cloud auth first (if configured)
        if (SupabaseConfig.isConfigured) {
            try {
                val cloudAuth = CloudAuthManager()
                cloudAuth.restoreSession()
                isLoggedIn = supabaseClient.auth.currentUserOrNull() != null
            } catch (_: Exception) {}
        }

        // Fall back to local auth check
        if (!isLoggedIn) {
            localAuth.restoreSession()
            isLoggedIn = localAuth.isLoggedIn
        }

        if (isLoggedIn) {
            navController.navigate(Dest.DASHBOARD) {
                popUpTo(Dest.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Dest.ONBOARDING) {
                popUpTo(Dest.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .graphicsLayer { alpha = fadeInAlpha },
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryColor.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant pulsing logo
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .border(2.dp, PrimaryContainerColor, RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OS",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TuitionOS",
                color = PrimaryColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Center Management Platform",
                color = OnSurfaceVariantColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Custom thin spinner ring
            CircularProgressIndicator(
                color = PrimaryColor.copy(alpha = 0.3f),
                trackColor = PrimaryColor.copy(alpha = 0.08f),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Setting up your dashboard...",
                color = OnSurfaceVariantColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "உங்கள் டேஷ்போர்டு தயாராகிறது...",
                color = OnSurfaceVariantColor.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UiSurface)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stylish curved top background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceContainerHigh, SurfaceContainerLow)
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA3LA-5tFebd3v46k37dvDKESZh70Y4VnMmuWIGwsTInhyFJ78b0tGDJYt6d43TL-ytkQ6cbx5O5lFflyb69P-8ItSG2rsIvJ6gx-GRcmX3Ox42y8QmVbG2o2qM057E18O508Msxa5_W_gvDkeqcwK2a5e_hXBrZBVWPkZj1WPoofJLneInY9pp5v6k_XO7ttN1lSG_JAiB06KcqFQVmPfKi3kMpUdqmnVRUR1n3Io8JwL0d0UuOMjDKGKjYictv_B_ImSdrG6Dtvga",
                    contentDescription = "Onboarding Smart Classroom",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TuitionOS",
                color = PrimaryColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Manage your tuition center like a pro.",
                color = OnBackgroundColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fee tracking, attendance, and parent updates — all in your pocket.",
                color = OnSurfaceVariantColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "உங்கள் சென்டர், ஸ்மார்ட் ஆக நடக்கும்",
                        color = PrimaryContainerColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate(Dest.LOGIN) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 3. LOGIN SCREEN
@Composable
fun LoginScreen(navController: NavController, viewModel: TuitionViewModel) {
    var details by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val localAuth = remember { LocalAuthManager(context) }
    val cloudAuth = remember { CloudAuthManager() }
    val scope = rememberCoroutineScope()
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val isCloudMode = SupabaseConfig.isConfigured
    val supabaseUrl = SupabaseConfig.SUPABASE_URL
    val isKeyPresent = SupabaseConfig.SUPABASE_KEY.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UiSurface)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header / Logo area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            AppLogo()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to TuitionOS",
                color = PrimaryColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Center Management Platform",
                color = OnSurfaceVariantColor,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Card login sheet
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            border = BorderStroke(1.dp, StatusInactive)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Field 1: User details
                Text(
                    text = "Email Address",
                    color = OnSurfaceColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "மின்னஞ்சல் முகவரி",
                    color = OnSurfaceVariantColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = { Text("Enter email...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "User Icon", tint = OutlineColor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = OutlineVariantColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Field 2: Password
                Text(
                    text = "Password",
                    color = OnSurfaceColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "கடவுச்சொல்",
                    color = OnSurfaceVariantColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter password...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon", tint = OutlineColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = OutlineVariantColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password notice
                if (!isSignUp) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (isCloudMode) {
                            Text(
                                text = "Forgot Password?",
                                color = PrimaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    if (details.isNotBlank()) {
                                        scope.launch {
                                            cloudAuth.resetPassword(details)
                                            Toast.makeText(context, "Reset link sent to $details", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Enter your email first", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = "Offline mode - no password reset",
                                color = OnSurfaceVariantColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Login/Signup Button
                Button(
                    onClick = {
                        if (details.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            val result = if (isCloudMode) {
                                // Cloud mode: use Supabase Auth
                                if (isSignUp) {
                                    cloudAuth.signUp(details, password, details.substringBefore("@")).map { }
                                } else {
                                    cloudAuth.signIn(details, password).map { }
                                }
                            } else {
                                // Offline mode: use local BCrypt auth
                                if (isSignUp) {
                                    localAuth.signUp(details, password, details.substringBefore("@"))
                                } else {
                                    localAuth.signIn(details, password)
                                }
                            }
                            isLoading = false
                            result.fold(
                                onSuccess = {
                                    Toast.makeText(context,
                                        if (isSignUp) "Account created successfully" else "Welcome back!",
                                        Toast.LENGTH_SHORT).show()
                                    // Initialize default settings on first sign-up
                                    if (isSignUp) {
                                        try {
                                            val repo = TuitionRepository(context)
                                            repo.insertSettings(SettingsEntity(
                                                orgName = "My Tuition Center",
                                                centerId = "CEN-${(1000..9999).random()}",
                                                contactPhone = "",
                                                upiId = "",
                                                language = "English",
                                                planName = "Free Tier",
                                                renewDate = "",
                                                maxStudents = 100,
                                                activeStaffCount = 1
                                            ))
                                        } catch (_: Exception) {}
                                    }
                                    // Trigger cloud data sync on successful login
                                    if (isCloudMode) {
                                        viewModel.syncFromCloud()
                                    }
                                    navController.navigate(Dest.DASHBOARD) {
                                        popUpTo(Dest.LOGIN) { inclusive = true }
                                    }
                                },
                                onFailure = { e ->
                                    Toast.makeText(context,
                                        if (isSignUp) "Signup failed: ${e.message}" else "Login failed: ${e.message}",
                                        Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isSignUp) "Sign Up" else "Login",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward Icon",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle between signup/login
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
                        color = PrimaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { isSignUp = !isSignUp }
                    )
                    Text(
                        text = if (isCloudMode) "☁ Cloud Mode" else "📱 Offline Mode",
                        color = if (isCloudMode) StatusSuccess else OnSurfaceVariantColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariantColor.copy(alpha = 0.5f))
                    Text(
                        text = "or connect with",
                        color = OutlineColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariantColor.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social connections (placeholders)
                Button(
                    onClick = {
                        Toast.makeText(context, "Google/WhatsApp Sign-In not supported in offline-first mode. Please use Email Sign Up.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
                    border = BorderStroke(1.dp, OutlineVariantColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = BrandWhatsapp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with WhatsApp",
                            color = OnSurfaceColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Google/WhatsApp Sign-In not supported in offline-first mode. Please use Email Sign Up.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
                    border = BorderStroke(1.dp, OutlineVariantColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google",
                            tint = OnSurfaceColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            color = OnSurfaceColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Don't have an account? Contact Sales",
            color = OnSurfaceVariantColor,
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* sales link */ }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Helper to Export Student Attendance and Fee Schedules to a standardized downloadable CSV report
fun exportTuitionDataToCsv(
    context: Context, 
    students: List<StudentEntity>, 
    attendance: List<AttendanceRecordEntity>, 
    fees: List<FeeHistoryEntity>
) {
    try {
        val attendanceCsv = StringBuilder()
        attendanceCsv.append("--- TUITIONAL ATTENDANCE DIRECTORY ---\n")
        attendanceCsv.append("Record ID,Batch ID,Date Entered,Student Name,Marked Status\n")
        attendance.forEach { record ->
            val statusStr = if (record.isPresent) "Present" else "Absent"
            val escapedName = record.studentName.replace("\"", "\"\"")
            attendanceCsv.append("${record.id},${record.batchId},${record.date},\"$escapedName\",$statusStr\n")
        }

        val feesCsv = StringBuilder()
        feesCsv.append("\n--- TUITIONAL FEE SCHEDULES DIRECTORY ---\n")
        feesCsv.append("Fee ID,Student Name,Billing Month,installment Title,Billed Amount (INR),Payment Status,Scheduled Due Date,Outstanding Balance (INR)\n")
        fees.forEach { fee ->
            val escapedName = fee.studentName.replace("\"", "\"\"")
            val escapedInst = fee.installment.replace("\"", "\"\"")
            feesCsv.append("${fee.id},\"$escapedName\",${fee.month},\"$escapedInst\",${fee.amount},${fee.status},${fee.dueDate},${fee.outstandingBalance}\n")
        }

        val fullReportBytes = (attendanceCsv.toString() + feesCsv.toString()).toByteArray(charset("UTF-8"))

        // Create standard file inside cache path using the configured FileProvider authority
        val reportFile = java.io.File(context.cacheDir, "TuitionOS_Admin_Report.csv")
        reportFile.writeBytes(fullReportBytes)

        val reportUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.example.provider",
            reportFile
        )

        val csvIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "TuitionOS Administrative Data Export")
            putExtra(Intent.EXTRA_STREAM, reportUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Share Sheet Trigger
        context.startActivity(Intent.createChooser(csvIntent, "Save or Email CSV Administrative Export"))
    } catch (e: Exception) {
        Toast.makeText(context, "Administrative CSV export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

// 4. OWNER DASHBOARD
@Composable
fun DashboardScreen(navController: NavController, viewModel: TuitionViewModel) {
    val studentsState by viewModel.students.collectAsState()
    val batchesState by viewModel.batches.collectAsState()
    val leadsState by viewModel.leads.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val attendanceRecordsState by viewModel.attendanceRecords.collectAsState()
    val feeHistoryState by viewModel.feeHistory.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Calculate sum of active payments and pending payments
    val totalCollected = studentsState.filter { it.status == "Paid" }.sumOf { it.monthlyFee }
    val totalPending = studentsState.filter { it.status == "Pending" }.sumOf { it.monthlyFee }
    val totalOverdue = studentsState.filter { it.status == "Overdue" }.sumOf { it.monthlyFee }

    // Dynamic Accumulative revenue from history payments
    val totalHistoryRevenue = feeHistoryState.filter { it.status == "Paid" }.sumOf { it.amount }

    // Dynamic attendance grouping by Month
    val monthlyAttendanceMap = remember(attendanceRecordsState) {
        attendanceRecordsState.groupBy {
            if (it.date.length >= 7) {
                val rawMonth = it.date.substring(0, 7)
                try {
                    val sdfIn = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    sdfOut.format(sdfIn.parse(rawMonth) ?: Date())
                } catch(e: Exception) {
                    rawMonth
                }
            } else {
                "June 2026"
            }
        }.mapValues { (_, records) ->
            val presentCount = records.count { it.isPresent }
            val totalCount = records.size
            if (totalCount > 0) (presentCount * 100) / totalCount else 100
        }
    }

    // Dynamic revenue grouping by Month string
    val monthlyRevenueMap = remember(feeHistoryState) {
        feeHistoryState.filter { it.status == "Paid" }.groupBy { it.month }.mapValues { (_, fees) ->
            fees.sumOf { it.amount }
        }
    }

    Scaffold(
        topBar = {
            val pendingCount = studentsState.count { it.status == "Pending" || it.status == "Overdue" }
            DashboardTopBar(
                settingsState?.orgName ?: "Victory Academy",
                notificationCount = pendingCount,
                onSettingsClick = { navController.navigate(Dest.SETTINGS) }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = Dest.DASHBOARD)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Dest.CREATE_BATCH) },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Batch")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Welcome banner
            Column {
                Text(
                    text = "Welcome back, Center Admin",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Text(
                    text = "Here is your summary for today.",
                    fontSize = 14.sp,
                    color = OnSurfaceVariantColor
                )
            }

            // Summary metrics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Students Card
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                     modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Students",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STUDENTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${studentsState.size}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "மொத்த மாணவர்கள்",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Collected Card
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = Color.White),
                     border = BorderStroke(1.dp, StatusInactive),
                     modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Collected",
                                tint = StatusSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COLLECTED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariantColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${(totalCollected / 1000).toInt()}K",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "இன்று வசூலிக்கப்பட்டது",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantColor
                        )
                    }
                }
            }

            // Second row: Pending + Overdue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, StatusInactive),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = "Pending",
                                tint = StatusWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariantColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${((totalPending) / 1000).toInt()}K",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "நிலுவையில் உள்ளது",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantColor
                        )
                    }
                }

                // Overdue Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, StatusError.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overdue",
                                tint = StatusError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OVERDUE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusError,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${(totalOverdue / 1000).toInt()}K",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusError
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "தாமதமான கட்டணம்",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantColor
                        )
                    }
                }
            }

            // Quick Actions section
            Column {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionItem(
                        icon = Icons.Default.PersonAdd,
                        label = "Add Student",
                        containerColor = PrimaryContainerColor.copy(alpha = 0.15f),
                        contentColor = PrimaryContainerColor
                    ) {
                        navController.navigate(Dest.STUDENT_LIST)
                    }

                    QuickActionItem(
                        icon = Icons.Default.HowToReg,
                        label = "Attendance",
                        containerColor = SecondaryContainerColor.copy(alpha = 0.3f),
                        contentColor = SecondaryColor
                    ) {
                        if (batchesState.isNotEmpty()) {
                            navController.navigate("batch_attendance/${batchesState.first().id}")
                        } else {
                            Toast.makeText(context, "Please create a batch first", Toast.LENGTH_SHORT).show()
                        }
                    }

                    QuickActionItem(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "Payment",
                        containerColor = SurfaceVariantColor,
                        contentColor = PrimaryColor
                    ) {
                        navController.navigate(Dest.FEES_SCHEDULE)
                    }

                    QuickActionItem(
                        icon = Icons.Default.Badge,
                        label = "Staff",
                        containerColor = TertiaryContainerColor.copy(alpha = 0.2f),
                        contentColor = TertiaryColor
                    ) {
                        navController.navigate(Dest.STAFF_DIRECTORY)
                    }

                    QuickActionItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        containerColor = SurfaceContainerHigh,
                        contentColor = OnSurfaceColor
                    ) {
                        navController.navigate(Dest.SETTINGS)
                    }
                }
            }

            // CENTRAL DATA & ANALYTICS REPORTING HUB
            var selectedReportMonth by remember { mutableStateOf("") }
            val reportMonths = remember(monthlyAttendanceMap, monthlyRevenueMap) {
                (monthlyAttendanceMap.keys + monthlyRevenueMap.keys).filter { it.isNotBlank() && it != "Unknown" }.distinct().sorted()
            }
            if (selectedReportMonth.isEmpty() && reportMonths.isNotEmpty()) {
                selectedReportMonth = reportMonths.last()
            } else if (selectedReportMonth.isEmpty()) {
                selectedReportMonth = "December 2023" // Fallback matching prepopulated seed month
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, StatusInactive), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXECUTIVE REPORTING CENTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Monthly Board Analytics",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                    }

                    // CSV Administrative Export Button
                    IconButton(
                        onClick = {
                            exportTuitionDataToCsv(context, studentsState, attendanceRecordsState, feeHistoryState)
                        },
                        modifier = Modifier
                            .background(PrimaryColor.copy(alpha = 0.1f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Excel/CSV Reports",
                            tint = PrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = StatusInactive, thickness = 1.dp)

                // High visual stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("All-Time Base Rev", fontSize = 11.sp, color = OnSurfaceVariantColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "₹${String.format("%,.0f", totalHistoryRevenue)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Avg Attendance", fontSize = 11.sp, color = OnSurfaceVariantColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalPresent = attendanceRecordsState.count { it.isPresent }
                        val totalAtt = attendanceRecordsState.size
                        val avgPercent = if (totalAtt > 0) (totalPresent * 100) / totalAtt else 93
                        Text(
                            "$avgPercent%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }

                // Month Selector Pills
                if (reportMonths.isNotEmpty()) {
                    Text(
                        text = "Filter Report Month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reportMonths.forEach { m ->
                            val isSelected = selectedReportMonth == m
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) PrimaryColor else SurfaceContainerHighest,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedReportMonth = m }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = m,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OnSurfaceColor
                                )
                            }
                        }
                    }
                }

                // Month specific analytics view
                val mAttendance = monthlyAttendanceMap[selectedReportMonth] ?: 93
                val mRevenue = monthlyRevenueMap[selectedReportMonth] ?: 0.0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UiSurface),
                    border = BorderStroke(1.dp, StatusInactive)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Metrics for $selectedReportMonth",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceColor
                            )
                            Text(
                                text = "DYNAMIC REPORT",
                                fontSize = 9.sp,
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(PrimaryColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Monthly Revenue Collected", fontSize = 11.sp, color = OnSurfaceVariantColor)
                                Text("₹${String.format("%,.0f", mRevenue)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Attendance Percent", fontSize = 11.sp, color = OnSurfaceVariantColor)
                                Text("$mAttendance%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            }
                        }

                        // Attendance linear target indicator
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Target Achievement", fontSize = 10.sp, color = OnSurfaceVariantColor)
                                Text("$mAttendance%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { mAttendance / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (mAttendance >= 85) StatusSuccess else StatusWarning,
                                trackColor = SurfaceContainerHighest
                            )
                        }
                    }
                }

                // Administrative Report Download Trigger Button
                Button(
                    onClick = {
                        exportTuitionDataToCsv(context, studentsState, attendanceRecordsState, feeHistoryState)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download administrative CSV Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 30-Day Daily Attendance Trend Chart (Recharts equivalent)
            AttendanceTrendLineChart(attendanceRecords = attendanceRecordsState)

            // Today's Batches
            Column {
                Text(
                    text = "Today's Batches",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (batchesState.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active batches found. Create one!", color = OutlineColor)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        batchesState.take(2).forEach { batch ->
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, StatusInactive),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = batch.name,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurfaceColor
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val dynamicCount = studentsState.count { it.batchName == batch.name }
                                            Text(
                                                text = "${batch.startTime} - ${batch.endTime} • $dynamicCount Students",
                                                fontSize = 13.sp,
                                                color = OnSurfaceVariantColor
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = SurfaceVariantColor,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = batch.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { navController.navigate("batch_attendance/${batch.id}") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HowToReg,
                                                contentDescription = "Attendance",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Mark Attendance", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Absences (Dynamic from attendance records)
            Column {
                val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
                val todayAbsences = remember(attendanceRecordsState) {
                    attendanceRecordsState.filter { !it.isPresent && it.date == todayStr }
                }
                val recentAbsences = if (todayAbsences.isNotEmpty()) todayAbsences
                    else attendanceRecordsState.filter { !it.isPresent }.takeLast(3)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Absences",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    if (recentAbsences.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = ErrorContainerColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${recentAbsences.size} PENDING ALERTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnErrorContainerColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (recentAbsences.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent absences. Great attendance!",
                                color = StatusSuccess,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentAbsences.take(3).forEach { absence ->
                            val studentForAbsence = studentsState.find { it.fullName == absence.studentName }
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, StatusInactive),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(ErrorContainerColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonOff,
                                                contentDescription = "Absent Icon",
                                                tint = OnErrorContainerColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = absence.studentName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurfaceColor
                                            )
                                            Text(
                                                text = studentForAbsence?.standard ?: "Student",
                                                fontSize = 13.sp,
                                                color = OnSurfaceVariantColor
                                            )
                                        }
                                    }

                                    val parentPhone = studentForAbsence?.parentPhone ?: ""
                                    if (parentPhone.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val msg = WhatsAppTemplates.absentNotification(absence.studentName, absence.date)
                                                    val waLink = WhatsAppTemplates.buildWaLink(parentPhone, msg)
                                                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(waLink) }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(BrandWhatsapp, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                                contentDescription = "WhatsApp",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pressAnimation(onClick = onClick)
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(containerColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantColor,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 5. STUDENT LIST SCREEN
@Composable
fun StudentListScreen(navController: NavController, viewModel: TuitionViewModel) {
    val studentsState by viewModel.students.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }

    var showAddDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }

    Scaffold(
        topBar = {
            DashboardTopBar("TuitionOS", onSettingsClick = { navController.navigate(Dest.SETTINGS) })
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = Dest.STUDENT_LIST)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = "Student List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor
            )

            // Search and Status Dropdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search students... (மாணவர்கள்)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = OutlineVariantColor
                    )
                )

                // Status Filter Dropdown Component
                var dropdownExpanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("All Status", "Paid", "Pending", "Overdue")

                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = PrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = selectedStatusFilter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceColor
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand Status",
                            tint = OnSurfaceVariantColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, fontSize = 14.sp) },
                                onClick = {
                                    selectedStatusFilter = status
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "10th Std", "12th Std", "Active", "Overdue", "Pending")
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) PrimaryColor else Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryColor else OutlineVariantColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else OnSurfaceVariantColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Student List Filtering
            val filteredStudents = remember(studentsState, searchQuery, selectedFilter, selectedStatusFilter) {
                studentsState.filter { student ->
                    val matchesSearch = student.fullName.contains(searchQuery, ignoreCase = true) ||
                            student.standard.contains(searchQuery, ignoreCase = true)
                    
                    val matchesFilter = when (selectedFilter) {
                        "10th Std" -> student.standard.contains("10th")
                        "12th Std" -> student.standard.contains("12th")
                        "Active" -> student.status == "Active" || student.status == "Paid"
                        "Overdue" -> student.status == "Overdue"
                        "Pending" -> student.status == "Pending"
                        else -> true
                    }

                    val matchesDropdownFilter = when (selectedStatusFilter) {
                        "Paid" -> student.status.equals("Paid", ignoreCase = true)
                        "Pending" -> student.status.equals("Pending", ignoreCase = true)
                        "Overdue" -> student.status.equals("Overdue", ignoreCase = true)
                        else -> true
                    }

                    matchesSearch && matchesFilter && matchesDropdownFilter
                }
            }

            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students found matching your criteria", color = OutlineColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredStudents) { student ->
                        StudentCard(
                            student = student,
                            onClick = {
                                navController.navigate("student_profile/${student.id}")
                            },
                            onWhatsAppClick = {
                                try {
                                    val msg = WhatsAppTemplates.feeReminder(
                                        studentName = student.fullName,
                                        amount = student.monthlyFee,
                                        dueDate = "end of month",
                                        outstandingBalance = student.monthlyFee
                                    )
                                    val urlStr = WhatsAppTemplates.buildWaLink(student.parentPhone, msg)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onActionClick = {
                                if (student.status == "Paid") {
                                    Toast.makeText(context, "Already Paid. Viewing Receipt...", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateStudentStatus(student, "Paid")
                                    Toast.makeText(context, "Marked ${student.fullName} as Paid", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEditClick = {
                                studentToEdit = student
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditStudentDialog(
            student = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, std, pName, pPhone, stat, fee, avatar ->
                viewModel.addStudent(
                    fullName = name,
                    standard = std,
                    parentName = pName,
                    parentPhone = pPhone,
                    monthlyFee = fee,
                    batchName = "General",
                    status = stat,
                    avatarUrl = avatar
                )
                showAddDialog = false
                Toast.makeText(context, "Student registered successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (studentToEdit != null) {
        AddEditStudentDialog(
            student = studentToEdit,
            onDismiss = { studentToEdit = null },
            onSave = { name, std, pName, pPhone, stat, fee, avatar ->
                val updated = studentToEdit?.copy(
                    fullName = name,
                    standard = std,
                    parentName = pName,
                    parentPhone = pPhone,
                    status = stat,
                    monthlyFee = fee,
                    avatarUrl = avatar
                )
                if (updated != null) {
                    viewModel.updateStudent(updated)
                }
                studentToEdit = null
                Toast.makeText(context, "Student records updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun StudentCard(
    student: StudentEntity,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onActionClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val statusBgColor = when (student.status) {
        "Paid" -> Color(0xFFE6F4EA)
        "Overdue" -> ErrorContainerColor
        else -> Color(0xFFFEF3C7)
    }
    val statusTextColor = when (student.status) {
        "Paid" -> Color(0xFF137333)
        "Overdue" -> OnErrorContainerColor
        else -> Color(0xFF92400E)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, StatusInactive),
        modifier = Modifier
            .fillMaxWidth()
            .pressAnimation(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (student.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = student.avatarUrl,
                            contentDescription = student.fullName,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, StatusInactive, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = student.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "batch",
                                tint = OnSurfaceVariantColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = student.standard,
                                fontSize = 12.sp,
                                color = OnSurfaceVariantColor
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusBgColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = student.status.uppercase(),
                            color = statusTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Student Details",
                            tint = PrimaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (student.status == "Paid") Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.Payments,
                            contentDescription = "Fee action",
                            tint = if (student.status == "Paid") OnSurfaceVariantColor else PrimaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (student.status == "Paid") "View Receipt" else "Collect Fee",
                            color = if (student.status == "Paid") OnSurfaceVariantColor else PrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Button(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F9F0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp action",
                            tint = BrandWhatsapp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (student.status == "Pending") "Reminder" else "WhatsApp",
                            color = BrandWhatsapp,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Stateful Student Management Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    student: StudentEntity? = null,
    onDismiss: () -> Unit,
    onSave: (fullName: String, standard: String, parentName: String, parentPhone: String, status: String, monthlyFee: Double, avatarUrl: String) -> Unit
) {
    var fullName by remember { mutableStateOf(student?.fullName ?: "") }
    var standard by remember { mutableStateOf(student?.standard ?: "10th Std") }
    var parentName by remember { mutableStateOf(student?.parentName ?: "") }
    var parentPhone by remember { mutableStateOf(student?.parentPhone ?: "") }
    var status by remember { mutableStateOf(student?.status ?: "Pending") }
    var monthlyFeeStr by remember { mutableStateOf(student?.monthlyFee?.toString() ?: "2000") }
    var avatarUrl by remember { mutableStateOf(student?.avatarUrl ?: "") }

    // Validation errors
    val nameErr = V.requiredFieldError(fullName, "Student name") ?: V.maxLengthError(fullName, V.MAX_NAME_LENGTH, "Student name")
    val stdErr = V.requiredFieldError(standard, "Class") ?: V.maxLengthError(standard, V.MAX_STANDARD_LENGTH, "Class")
    val parentErr = V.maxLengthError(parentName, V.MAX_NAME_LENGTH, "Parent name")
    val phoneErr = V.phoneRequiredError(parentPhone)
    val feeErr = V.amountError(monthlyFeeStr)
    val isValid = nameErr == null && stdErr == null && parentErr == null && phoneErr == null && feeErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (student == null) "Register New Student" else "Edit Student Details",
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Picker
                ProfilePhotoPicker(
                    currentPhotoPath = avatarUrl.ifEmpty { null },
                    onPhotoSelected = { avatarUrl = it }
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.take(V.MAX_NAME_LENGTH) },
                    label = { Text("Full Name (முழு பெயர்) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = nameErr != null,
                    supportingText = nameErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = standard,
                    onValueChange = { standard = it.take(V.MAX_STANDARD_LENGTH) },
                    label = { Text("Standard / Class (வகுப்பு) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = stdErr != null,
                    supportingText = stdErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it.take(V.MAX_NAME_LENGTH) },
                    label = { Text("Parent / Guardian Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = parentErr != null,
                    supportingText = parentErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text("Contact Number (தொடர்பு எண்) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    isError = phoneErr != null,
                    supportingText = phoneErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = monthlyFeeStr,
                    onValueChange = { monthlyFeeStr = it.filter { c -> c.isDigit() || c == '.' }.take(V.MAX_AMOUNT_LENGTH) },
                    label = { Text("Monthly Tuition Fee (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = feeErr != null,
                    supportingText = feeErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                Text("Enrollment / Payment Status:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statuses = listOf("Active", "Paid", "Pending", "Overdue")
                    statuses.forEach { s ->
                        val isSelected = status == s
                        Button(
                            onClick = { status = s },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryColor else Color.White,
                                contentColor = if (isSelected) Color.White else OnSurfaceVariantColor
                            ),
                            border = BorderStroke(1.dp, if (isSelected) PrimaryColor else StatusInactive),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(s, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = V.parseAmount(monthlyFeeStr, 2000.0)
                    onSave(fullName.trim(), standard.trim(), parentName.trim(), parentPhone.trim(), status, fee, avatarUrl)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Save to DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OutlineColor)
            }
        }
    )
}

// 6. STUDENT PROFILE SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(navController: NavController, viewModel: TuitionViewModel, studentId: Int) {
    val studentsState by viewModel.students.collectAsState()
    val student = studentsState.find { it.id == studentId }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Fee history collected at top-level to avoid recomposition issues
    val studentName = student?.fullName ?: ""
    val feeHistoryFlow = remember(studentName) {
        if (studentName.isNotEmpty()) viewModel.getFeeHistoryForStudent(studentName)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val studentFeeHistory by feeHistoryFlow.collectAsState(initial = emptyList())

    if (student == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Student not found", color = OutlineColor)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Profile Identity Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(64.dp)) {
                        if (student.avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = student.avatarUrl,
                                contentDescription = student.fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.dp, StatusInactive, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SurfaceContainerHighest, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.fullName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        // Success check badge
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(StatusSuccess, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = student.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID: ${student.studentId}", fontSize = 12.sp, color = OnSurfaceVariantColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(4.dp).background(OutlineVariantColor, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(student.standard, fontSize = 12.sp, color = OnSurfaceVariantColor)
                        }
                    }
                }
            }

            // Quick Actions Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val msg = WhatsAppTemplates.feeReminder(
                                studentName = student.fullName,
                                amount = student.monthlyFee,
                                dueDate = "this month",
                                outstandingBalance = student.monthlyFee
                            )
                            val waLink = WhatsAppTemplates.buildWaLink(student.parentPhone, msg)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waLink))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandWhatsapp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp", fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.parentPhone}"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call Parent", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Parent", fontSize = 14.sp)
                    }
                }
            }

            // Batch Information
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BATCH INFORMATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariantColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "தொகுதி தகவல்",
                    fontSize = 12.sp,
                    color = OnSurfaceVariantColor.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Default
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, StatusInactive),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = student.batchName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Physics, Chemistry, Biology",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariantColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(PrimaryContainerColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = "Time",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("5:00 PM - 7:00 PM", fontSize = 13.sp, color = OnSurfaceVariantColor)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Days",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mon, Wed, Fri", fontSize = 13.sp, color = OnSurfaceVariantColor)
                            }
                        }
                    }
                }
            }

            // Academic Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ACADEMIC PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariantColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "கல்வி முன்னேற்றம்",
                    fontSize = 12.sp,
                    color = OnSurfaceVariantColor.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Default
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Attendance Grid
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceContainerLow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = "Attendance",
                                    tint = PrimaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${student.attendancePercentage}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Attendance", fontSize = 13.sp, color = OnSurfaceColor)
                            Text("வருகை", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        }
                    }

                    // Test Score Grid
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceContainerLow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = "Quiz",
                                    tint = TertiaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${student.termMidTestScore}/100",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Term Mid-Test", fontSize = 13.sp, color = OnSurfaceColor)
                            Text("இடைத் தேர்வு", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        }
                    }
                }
            }

            // Fee History
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FEE HISTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariantColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "கட்டண வரலாறு",
                            fontSize = 12.sp,
                            color = OnSurfaceVariantColor.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Default
                        )
                    }

                    Text(
                        text = "View Ledger",
                        color = PrimaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* action */ }
                    )
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, StatusInactive),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (studentFeeHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No fee records registered yet", color = OutlineColor)
                            }
                        } else {
                            studentFeeHistory.forEachIndexed { index, fee ->
                                FeeHistoryItemRow(fee = fee, itemIndex = index)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Student Dialog
    if (showEditDialog) {
        AddEditStudentDialog(
            student = student,
            onDismiss = { showEditDialog = false },
            onSave = { name, std, pName, pPhone, stat, fee, avatar ->
                val updated = student.copy(
                    fullName = name, standard = std,
                    parentName = pName, parentPhone = pPhone,
                    status = stat, monthlyFee = fee, avatarUrl = avatar
                )
                viewModel.updateStudent(updated)
                showEditDialog = false
                Toast.makeText(context, "Student updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorColor) },
            title = { Text("Delete Student?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${student.fullName}? This will also remove their fee history and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStudent(student)
                    showDeleteConfirm = false
                    Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }) {
                    Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = OutlineColor)
                }
            }
        )
    }
}

@Composable
fun FeeHistoryItemRow(fee: FeeHistoryEntity, itemIndex: Int) {
    val statusBgColor = when (fee.status) {
        "Paid" -> Color(0xFFE6F4EA)
        "Overdue" -> ErrorContainerColor
        else -> Color(0xFFFEF3C7)
    }
    val statusTextColor = when (fee.status) {
        "Paid" -> Color(0xFF137333)
        "Overdue" -> OnErrorContainerColor
        else -> Color(0xFF92400E)
    }
    val statusTamilText = when (fee.status) {
        "Paid" -> "செலுத்தப்பட்டது"
        "Overdue" -> "தாமதம்"
        else -> "நிலுவையில்"
    }

    Column {
        if (itemIndex > 0) {
            HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.4f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = fee.month,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${fee.amount.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (fee.status == "Overdue") ErrorColor else OnSurfaceColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${fee.installment}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariantColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (fee.status) {
                                "Paid" -> Icons.Default.CheckCircle
                                "Overdue" -> Icons.Default.Warning
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = fee.status,
                            tint = statusTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = fee.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusTamilText,
                    fontSize = 11.sp,
                    color = statusTextColor.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

// 7. CREATE BATCH SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchScreen(navController: NavController, viewModel: TuitionViewModel) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var daysSelected by remember { mutableStateOf(mutableListOf<String>()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val nameErr = V.requiredFieldError(name, "Batch name") ?: V.maxLengthError(name, V.MAX_BATCH_NAME_LENGTH, "Batch name")
    val subjectErr = V.requiredFieldError(subject, "Subject") ?: V.maxLengthError(subject, V.MAX_SUBJECT_LENGTH, "Subject")
    val startErr = if (startTime.isNotBlank() && !Regex("^\\d{1,2}:\\d{2}\$").matches(startTime)) "Enter valid time (HH:MM)" else null
    val endErr = if (endTime.isNotBlank() && !Regex("^\\d{1,2}:\\d{2}\$").matches(endTime)) "Enter valid time (HH:MM)" else null
    val batchValid = nameErr == null && subjectErr == null && startErr == null && endErr == null
    val batchesState by viewModel.batches.collectAsState()
    val studentsState by viewModel.students.collectAsState()
    var batchToDelete by remember { mutableStateOf<BatchEntity?>(null) }
    var batchToEdit by remember { mutableStateOf<BatchEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Batch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "புதிய வகுப்பை உருவாக்கு",
                fontSize = 15.sp,
                color = OnSurfaceVariantColor,
                fontFamily = FontFamily.Default
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Batch Name
                    Column {
                        Text("BATCH NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, letterSpacing = 0.5.sp)
                        Text("வகுப்பு பெயர்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(V.MAX_BATCH_NAME_LENGTH) },
                            placeholder = { Text("e.g., 10th Maths") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            isError = nameErr != null,
                            supportingText = nameErr?.let { { Text(it, color = ErrorColor) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = OutlineVariantColor
                            )
                        )
                    }

                    // Subject
                    Column {
                        Text("SUBJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, letterSpacing = 0.5.sp)
                        Text("பாடம்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it.take(V.MAX_SUBJECT_LENGTH) },
                            placeholder = { Text("Select Subject") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            isError = subjectErr != null,
                            supportingText = subjectErr?.let { { Text(it, color = ErrorColor) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = OutlineVariantColor
                            )
                        )
                    }

                    // Days of week
                    Column {
                        Text("DAYS OF WEEK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, letterSpacing = 0.5.sp)
                        Text("நாட்கள்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        Spacer(modifier = Modifier.height(8.dp))
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            days.forEach { day ->
                                val isSelected = daysSelected.contains(day)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) PrimaryColor else Color.White,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) PrimaryColor else OutlineVariantColor,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            val current = daysSelected.toMutableList()
                                            if (isSelected) current.remove(day) else current.add(day)
                                            daysSelected = current
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = day,
                                        color = if (isSelected) Color.White else OnSurfaceVariantColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Start/End time pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("START TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, letterSpacing = 0.5.sp)
                            Text("தொடங்கும் நேரம்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it.take(5) },
                                placeholder = { Text("--:--") },
                                isError = startErr != null,
                                supportingText = startErr?.let { { Text(it, color = ErrorColor) } },
                                trailingIcon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "time") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = OutlineVariantColor
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("END TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, letterSpacing = 0.5.sp)
                            Text("முடியும் நேரம்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it.take(5) },
                                placeholder = { Text("--:--") },
                                isError = endErr != null,
                                supportingText = endErr?.let { { Text(it, color = ErrorColor) } },
                                trailingIcon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "time") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = OutlineVariantColor
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit
                    Button(
                        onClick = {
                            viewModel.addBatch(
                                name = name.trim(),
                                subject = subject.trim(),
                                daysOfWeek = daysSelected.joinToString(", "),
                                startTime = if (startTime.isNotEmpty()) startTime.trim() else "05:00 PM",
                                endTime = if (endTime.isNotEmpty()) endTime.trim() else "07:00 PM"
                            )
                            Toast.makeText(context, "Batch Created Successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        enabled = batchValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "create")
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Create Batch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("உருவாக்கு", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontFamily = FontFamily.Default)
                            }
                        }
                    }
                }
            }

            // Existing Batches Section
            if (batchesState.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Existing Batches / இருக்கும் வகுப்புகள்",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )

                batchesState.forEach { batch ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressAnimation(onClick = { navController.navigate("batch_attendance/${batch.id}") })
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val batchStudentCount = studentsState.count { it.batchName == batch.name }
                                    Text(batch.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                    Text("${batch.startTime} - ${batch.endTime} • $batchStudentCount Students", fontSize = 12.sp, color = OnSurfaceVariantColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(SurfaceVariantColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(batch.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { batchToEdit = batch }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, "Edit batch", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { batchToDelete = batch }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, "Delete batch", tint = ErrorColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Batch Delete Confirmation
    if (batchToDelete != null) {
        AlertDialog(
            onDismissRequest = { batchToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = ErrorColor) },
            title = { Text("Delete Batch?", fontWeight = FontWeight.Bold) },
            text = { Text("Delete '${batchToDelete!!.name}'? Students assigned to this batch will not be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBatch(batchToDelete!!)
                    batchToDelete = null
                    Toast.makeText(context, "Batch deleted", Toast.LENGTH_SHORT).show()
                }) { Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { batchToDelete = null }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }

    // Batch Edit Dialog
    if (batchToEdit != null) {
        var editName by remember(batchToEdit) { mutableStateOf(batchToEdit!!.name) }
        var editSubject by remember(batchToEdit) { mutableStateOf(batchToEdit!!.subject) }
        var editStart by remember(batchToEdit) { mutableStateOf(batchToEdit!!.startTime) }
        var editEnd by remember(batchToEdit) { mutableStateOf(batchToEdit!!.endTime) }
        val editNameErr = V.requiredFieldError(editName, "Batch name") ?: V.maxLengthError(editName, V.MAX_BATCH_NAME_LENGTH, "Batch name")
        val editSubjectErr = V.requiredFieldError(editSubject, "Subject") ?: V.maxLengthError(editSubject, V.MAX_SUBJECT_LENGTH, "Subject")
        val editStartErr = if (editStart.isNotBlank() && !Regex("^\\d{1,2}:\\d{2}\$").matches(editStart)) "Enter valid time (HH:MM)" else null
        val editEndErr = if (editEnd.isNotBlank() && !Regex("^\\d{1,2}:\\d{2}\$").matches(editEnd)) "Enter valid time (HH:MM)" else null
        val editValid = editNameErr == null && editSubjectErr == null && editStartErr == null && editEndErr == null
        AlertDialog(
            onDismissRequest = { batchToEdit = null },
            icon = { Icon(Icons.Default.Edit, null, tint = PrimaryColor) },
            title = { Text("Edit Batch", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it.take(V.MAX_BATCH_NAME_LENGTH) }, label = { Text("Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = editNameErr != null, supportingText = editNameErr?.let { { Text(it, color = ErrorColor) } })
                    OutlinedTextField(value = editSubject, onValueChange = { editSubject = it.take(V.MAX_SUBJECT_LENGTH) }, label = { Text("Subject *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = editSubjectErr != null, supportingText = editSubjectErr?.let { { Text(it, color = ErrorColor) } })
                    OutlinedTextField(value = editStart, onValueChange = { editStart = it.take(5) }, label = { Text("Start Time") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = editStartErr != null, supportingText = editStartErr?.let { { Text(it, color = ErrorColor) } })
                    OutlinedTextField(value = editEnd, onValueChange = { editEnd = it.take(5) }, label = { Text("End Time") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = editEndErr != null, supportingText = editEndErr?.let { { Text(it, color = ErrorColor) } })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updated = batchToEdit!!.copy(name = editName.trim(), subject = editSubject.trim(), startTime = editStart.trim(), endTime = editEnd.trim())
                    viewModel.updateBatch(updated)
                    batchToEdit = null
                    Toast.makeText(context, "Batch updated!", Toast.LENGTH_SHORT).show()
                }, enabled = editValid) { Text("Save", color = PrimaryColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { batchToEdit = null }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }
}

// 8. BATCH ATTENDANCE SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAttendanceScreen(navController: NavController, viewModel: TuitionViewModel, batchId: Int) {
    val batchesState by viewModel.batches.collectAsState()
    val studentsState by viewModel.students.collectAsState()
    val attendanceRecordsState by viewModel.attendanceRecords.collectAsState()
    
    val batch = batchesState.find { it.id == batchId }
    val context = LocalContext.current

    // Set interactive date selection
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    
    // Switch between "Mark Attendance" and "Monthly Report"
    var activeTab by remember { mutableStateOf("Mark") }

    // Local presence state map to track present/absent checks
    val attendanceStates = remember { mutableStateMapOf<Int, Boolean>() }

    // Init present state / fetch matching saved record for specific batch & date
    LaunchedEffect(studentsState, batch, selectedDate, attendanceRecordsState) {
        if (batch != null) {
            val assigned = studentsState.filter {
                it.batchName == batch.name
            }
            val existing = attendanceRecordsState.filter {
                it.batchId == batchId && it.date == selectedDate
            }
            if (existing.isNotEmpty()) {
                assigned.forEach { student ->
                    val record = existing.find { it.studentName == student.fullName }
                    attendanceStates[student.id] = record?.isPresent ?: true
                }
            } else {
                assigned.forEach { student ->
                    if (!attendanceStates.containsKey(student.id)) {
                        attendanceStates[student.id] = true
                    }
                }
            }
        }
    }

    val absentCount = attendanceStates.values.count { !it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TuitionOS Attendance", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryColor)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryColor)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Batch Context Header
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BATCH NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantColor, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(batch?.name ?: "10th Maths Morning", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val parsed = sdf.parse(selectedDate) ?: return@IconButton
                                        val cal = Calendar.getInstance().apply {
                                            time = parsed
                                            add(Calendar.DAY_OF_YEAR, -1)
                                        }
                                        selectedDate = sdf.format(cal.time)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Day", tint = PrimaryColor)
                            }
                            
                            Text(
                                text = selectedDate, 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = OnSurfaceColor,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val parsed = sdf.parse(selectedDate) ?: return@IconButton
                                        val cal = Calendar.getInstance().apply {
                                            time = parsed
                                            add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                        selectedDate = sdf.format(cal.time)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day", tint = PrimaryColor)
                            }
                        }
                        Text("Selected Date", fontSize = 11.sp, color = OnSurfaceVariantColor)
                    }
                }
            }

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nowCalendar = Calendar.getInstance()
            val currentMinutes = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)
            val selectedIsToday = selectedDate == todayDate
            val batchStartMinutes = batch?.startTime?.let { parseTimeToMinutes(it) }
            val batchEndMinutes = batch?.endTime?.let { parseTimeToMinutes(it) }
            val graceEndMinutes = batchStartMinutes?.plus(10)
            val sessionClosed = selectedIsToday && batchEndMinutes != null && currentMinutes > batchEndMinutes
            val graceOpen = selectedIsToday && batchStartMinutes != null && currentMinutes <= (graceEndMinutes ?: 0)
            val attendanceWindowOpen = selectedIsToday && batchEndMinutes != null && currentMinutes <= batchEndMinutes

            // Tabs for Mark Attendance vs Monthly Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mark" to "Register Attendance", "History" to "Monthly History / Stats").forEach { (tabId, tabName) ->
                    val isSelected = activeTab == tabId
                    Button(
                        onClick = { activeTab = tabId },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PrimaryColor else Color.White,
                            contentColor = if (isSelected) Color.White else OnSurfaceVariantColor
                        ),
                        border = BorderStroke(1.dp, if (isSelected) PrimaryColor else StatusInactive),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(tabName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeTab == "Mark") {
                if (batch != null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Session timing", fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${batch.startTime} - ${batch.endTime} (+10 min grace)", fontSize = 13.sp, color = OnSurfaceVariantColor)
                            if (selectedIsToday) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (sessionClosed) "Attendance window is closed for today. Late marks will count as absent." else if (graceOpen) "Attendance grace period active until ${formatGraceWindow(batch.startTime)}." else "Attendance open until ${batch.endTime}.",
                                    fontSize = 12.sp,
                                    color = if (sessionClosed) StatusError else StatusSuccess
                                )
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Recording historical attendance for selected date.", fontSize = 12.sp, color = OnSurfaceVariantColor)
                            }
                        }
                    }
                }
                // Interactive register UI
                if (absentCount > 0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                        border = BorderStroke(1.dp, OutlineVariantColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = StatusWarning)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$absentCount students absent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                    Text("Notify parents via WhatsApp?", fontSize = 14.sp, color = OnSurfaceVariantColor)
                                }
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val absentStudentIds = attendanceStates.filter { !it.value }.keys.toList()
                                        absentStudentIds.take(3).forEach { studentId ->
                                            val student = studentsState.find { it.id == studentId }
                                            val studentName = student?.fullName ?: "Student"
                                            val parentPhone = student?.parentPhone ?: ""
                                            if (parentPhone.isNotBlank()) {
                                                val msg = WhatsAppTemplates.absentNotification(studentName, selectedDate)
                                                val waLink = WhatsAppTemplates.buildWaLink(parentPhone, msg)
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waLink))
                                                context.startActivity(intent)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandWhatsapp, CircleShape)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Student List for marking
                val assignedStudents = studentsState.filter {
                    batch == null || it.batchName == batch.name
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(assignedStudents) { student ->
                        val isPresent = attendanceStates[student.id] ?: true

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, StatusInactive)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (student.avatarUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = student.avatarUrl,
                                            contentDescription = student.fullName,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, StatusInactive, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(SurfaceContainerHighest, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.fullName.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = student.fullName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceColor
                                        )
                                        Text(
                                            text = student.standard,
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariantColor
                                        )
                                    }
                                }

                                // Interactive Checkbox/Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { attendanceStates[student.id] = false },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                color = if (!isPresent) StatusError else StatusInactive,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Absent",
                                            tint = if (!isPresent) Color.White else OnSurfaceVariantColor
                                        )
                                    }

                                    IconButton(
                                        onClick = { attendanceStates[student.id] = true },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                color = if (isPresent) StatusSuccess else StatusInactive,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Present",
                                            tint = if (isPresent) Color.White else OnSurfaceVariantColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Submit Attendance Bar
                Button(
                    onClick = {
                        viewModel.saveBatchAttendance(
                            batchId = batchId,
                            selectedDate = selectedDate,
                            attendanceStates = attendanceStates,
                            assignedStudents = assignedStudents,
                            currentRecords = attendanceRecordsState.filter { it.batchId == batchId && it.date == selectedDate }
                        )
                        Toast.makeText(context, "Attendance saved for $selectedDate!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Save / Commit Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

            } else {
                // Monthly History / Stats reports
                val assignedStudents = studentsState.filter {
                    batch == null || it.batchName == batch.name
                }
                
                // Group attendance for this batch by student
                val batchRecords = attendanceRecordsState.filter { it.batchId == batchId }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Monthly Summary Stats", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Total sessions taken: ${batchRecords.map { it.date }.distinct().size} days", fontSize = 13.sp)
                            }
                        }
                    }

                    if (assignedStudents.isEmpty()) {
                        item {
                            Text("No students in this batch", color = OutlineColor, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    } else {
                        items(assignedStudents) { student ->
                            val studentRecords = batchRecords.filter { it.studentName == student.fullName }
                            val totalDays = studentRecords.size
                            val presentDays = studentRecords.count { it.isPresent }
                            val percent = if (totalDays > 0) (presentDays * 100) / totalDays else 100

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, StatusInactive)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceColor)
                                        Text("Parent: ${student.parentName} (${student.parentPhone})", fontSize = 12.sp, color = OnSurfaceVariantColor)
                                        Text("Status: ${student.status}", fontSize = 12.sp, color = if (student.status == "Active") StatusSuccess else PrimaryColor)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("$percent%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (percent >= 80) StatusSuccess else StatusError)
                                        Text("$presentDays/$totalDays attended", fontSize = 11.sp, color = OnSurfaceVariantColor)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Clickable Days Taken this Month", fontWeight = FontWeight.Bold, color = OnSurfaceColor, fontSize = 14.sp)
                    }

                    val daysTaken = batchRecords.map { it.date }.distinct().sortedDescending()
                    if (daysTaken.isEmpty()) {
                        item {
                            Text("No history captured for this month yet. Use the Register tab above to submit.", fontSize = 12.sp, color = OutlineColor)
                        }
                    } else {
                        items(daysTaken) { day ->
                            val recordsForDay = batchRecords.filter { it.date == day }
                            val presentCount = recordsForDay.count { it.isPresent }
                            val totalCount = recordsForDay.size
                            
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighest),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedDate = day
                                    activeTab = "Mark"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date", tint = PrimaryColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(day, fontWeight = FontWeight.Medium, color = OnSurfaceColor)
                                    }
                                    Text("$presentCount/$totalCount Present", fontWeight = FontWeight.SemiBold, color = PrimaryColor, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. STAFF DIRECTORY SCREEN
@Composable
fun StaffDirectoryScreen(navController: NavController, viewModel: TuitionViewModel) {
    val staffState by viewModel.staff.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddStaff by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<StaffEntity?>(null) }
    var staffToDelete by remember { mutableStateOf<StaffEntity?>(null) }

    Scaffold(
        topBar = {
            DashboardTopBar("TuitionOS", onSettingsClick = { navController.navigate(Dest.SETTINGS) })
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStaff = true },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Staff")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Header Section
            Column {
                Text("Staff Directory", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "பணியாளர்கள் விவரம் • ${staffState.size} Active Members",
                    fontSize = 15.sp,
                    color = OnSurfaceVariantColor,
                    fontFamily = FontFamily.Default
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search staff or batches...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = OutlineVariantColor
                )
            )

            // Staff List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val filteredStaff = staffState.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true)
                }

                items(filteredStaff) { member ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (member.avatarUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = member.avatarUrl,
                                            contentDescription = member.name,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, SurfaceColor, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(SecondaryContainerColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.name.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = OnSecondaryContainerColor,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryContainerColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(member.role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(member.tamilRole, fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SurfaceContainerLow, CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }

                                    if (member.whatsapp.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${member.whatsapp}?text=Hello%20${member.name}."))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFE8F9F0), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = BrandWhatsapp, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    IconButton(
                                        onClick = { staffToEdit = member },
                                        modifier = Modifier.size(36.dp).background(Color(0xFFE3F2FD), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { staffToDelete = member },
                                        modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = ErrorColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (member.assignedBatches.isNotEmpty() || member.responsibilities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = SurfaceColor)
                                Spacer(modifier = Modifier.height(12.dp))

                                if (member.assignedBatches.isNotEmpty()) {
                                    Text("Assigned Batches", fontSize = 13.sp, color = OnSurfaceVariantColor)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        member.assignedBatches.split(",").forEach { batchName ->
                                            Box(
                                                modifier = Modifier
                                                    .background(UiSurface, RoundedCornerShape(8.dp))
                                                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(8.dp).background(StatusSuccess, CircleShape))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(batchName.trim(), fontSize = 13.sp, color = OnSurfaceColor)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text("Responsibilities", fontSize = 13.sp, color = OnSurfaceVariantColor)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        member.responsibilities.split(",").forEach { resp ->
                                            Box(
                                                modifier = Modifier
                                                    .background(UiSurface, RoundedCornerShape(8.dp))
                                                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(resp.trim(), fontSize = 13.sp, color = OnSurfaceColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Staff Dialog
    if (showAddStaff) {
        AddEditStaffDialog(
            staff = null,
            onDismiss = { showAddStaff = false },
            onSave = { name, role, tamilRole, batches, responsibilities, phone, whatsapp ->
                viewModel.addStaff(name, role, tamilRole, batches, responsibilities, phone, whatsapp)
                showAddStaff = false
                Toast.makeText(context, "Staff added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Staff Dialog
    if (staffToEdit != null) {
        AddEditStaffDialog(
            staff = staffToEdit,
            onDismiss = { staffToEdit = null },
            onSave = { name, role, tamilRole, batches, responsibilities, phone, whatsapp ->
                val updated = staffToEdit!!.copy(
                    name = name, role = role, tamilRole = tamilRole,
                    assignedBatches = batches, responsibilities = responsibilities,
                    phone = phone, whatsapp = whatsapp
                )
                viewModel.updateStaff(updated)
                staffToEdit = null
                Toast.makeText(context, "Staff updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Staff Confirmation
    if (staffToDelete != null) {
        AlertDialog(
            onDismissRequest = { staffToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = ErrorColor) },
            title = { Text("Delete Staff?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${staffToDelete!!.name} from the staff directory?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStaff(staffToDelete!!)
                    staffToDelete = null
                    Toast.makeText(context, "Staff removed", Toast.LENGTH_SHORT).show()
                }) { Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { staffToDelete = null }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }
}

@Composable
fun AddEditStaffDialog(
    staff: StaffEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, tamilRole: String, batches: String, responsibilities: String, phone: String, whatsapp: String) -> Unit
) {
    var name by remember(staff) { mutableStateOf(staff?.name ?: "") }
    var role by remember(staff) { mutableStateOf(staff?.role ?: "") }
    var tamilRole by remember(staff) { mutableStateOf(staff?.tamilRole ?: "") }
    var batches by remember(staff) { mutableStateOf(staff?.assignedBatches ?: "") }
    var responsibilities by remember(staff) { mutableStateOf(staff?.responsibilities ?: "") }
    var phone by remember(staff) { mutableStateOf(staff?.phone ?: "") }
    var whatsapp by remember(staff) { mutableStateOf(staff?.whatsapp ?: "") }

    val nameErr = V.requiredFieldError(name, "Name") ?: V.maxLengthError(name, V.MAX_NAME_LENGTH, "Name")
    val roleErr = V.requiredFieldError(role, "Role") ?: V.maxLengthError(role, V.MAX_ROLE_LENGTH, "Role")
    val phoneErr = V.phoneError(phone)
    val whatsappErr = V.phoneError(whatsapp)
    val isValid = nameErr == null && roleErr == null && phoneErr == null && whatsappErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (staff == null) Icons.Default.PersonAdd else Icons.Default.Edit, null, tint = PrimaryColor) },
        title = { Text(if (staff == null) "Add Staff" else "Edit Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(V.MAX_NAME_LENGTH) }, label = { Text("Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = nameErr != null, supportingText = nameErr?.let { { Text(it, color = ErrorColor) } })
                OutlinedTextField(value = role, onValueChange = { role = it.take(V.MAX_ROLE_LENGTH) }, label = { Text("Role (English) *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = roleErr != null, supportingText = roleErr?.let { { Text(it, color = ErrorColor) } })
                OutlinedTextField(value = tamilRole, onValueChange = { tamilRole = it.take(V.MAX_ROLE_LENGTH) }, label = { Text("Role (Tamil)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = batches, onValueChange = { batches = it.take(V.MAX_NAME_LENGTH) }, label = { Text("Assigned Batches") }, placeholder = { Text("Batch1, Batch2") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = responsibilities, onValueChange = { responsibilities = it.take(V.MAX_DESCRIPTION_LENGTH) }, label = { Text("Responsibilities") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = phoneErr != null, supportingText = phoneErr?.let { { Text(it, color = ErrorColor) } })
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("WhatsApp") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = whatsappErr != null, supportingText = whatsappErr?.let { { Text(it, color = ErrorColor) } })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name.trim(), role.trim(), tamilRole.trim(), batches.trim(), responsibilities.trim(), phone.trim(), whatsapp.trim())
            }, enabled = isValid) { Text("Save", color = PrimaryColor, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OutlineColor) }
        }
    )
}

// 10. ORGANIZATION SETTINGS SCREEN
@Composable
fun SettingsScreen(navController: NavController, viewModel: TuitionViewModel) {
    val settingsState by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showProfilePhotoPicker by remember { mutableStateOf(false) }
    var showOrgEditDialog by remember { mutableStateOf(false) }
    var showUpiEditDialog by remember { mutableStateOf(false) }

    // Profile photo picker launcher
    val profileGalleryLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = ProfilePhotoManager.savePhotoToInternalStorage(context, uri)
                if (path != null && settingsState != null) {
                    viewModel.insertSettings(settingsState!!.copy(profilePhotoPath = path))
                }
            }
        }
    }
    val profileCameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        // Camera capture handled via dialog
    }

    Scaffold(
        topBar = {
            DashboardTopBar("TuitionOS", onSettingsClick = { navController.navigate(Dest.SETTINGS) })
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Column {
                Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                Text(
                    text = "அமைப்புகள்",
                    fontSize = 12.sp,
                    color = OnSurfaceVariantColor,
                    fontFamily = FontFamily.Default
                )
            }

            // Primary Profile Info Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile photo with change capability
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryColor, CircleShape)
                                .pressAnimation(onClick = {
                                    // Show photo picker
                                })
                        ) {
                            if (settingsState?.profilePhotoPath?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = java.io.File(settingsState!!.profilePhotoPath),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "VA",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(settingsState?.orgName ?: "Victory Academy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            Text(
                                text = "விக்டரி அகாடமி",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantColor,
                                fontFamily = FontFamily.Default
                            )
                        }

                        IconButton(onClick = { showOrgEditDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit org details", tint = PrimaryColor)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Center ID / மைய எண்", fontSize = 11.sp, color = OutlineColor, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(SurfaceColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(settingsState?.centerId ?: "CEN-8492-TN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Primary Contact / தொடர்பு எண்", fontSize = 11.sp, color = OutlineColor, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(settingsState?.contactPhone ?: "+91 98765 43210", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                        }
                    }

                    Button(
                        onClick = {
                            profileGalleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile", color = PrimaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Payment Settings
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SecondaryContainerColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "payments", tint = OnSecondaryContainerColor, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Payment Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                Text("கட்டண அமைப்புகள்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE6F4EA), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "qr", tint = OnSecondaryContainerColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Primary UPI ID / முதன்மை UPI ID", fontSize = 11.sp, color = OutlineColor, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    Text(settingsState?.upiId ?: "victoryacademy@okbank", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                }
                            }

                            IconButton(onClick = {
                                showUpiEditDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.SyncAlt, contentDescription = "sync", tint = PrimaryColor)
                            }
                        }
                    }
                }
            }

            // Language Selection
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(TertiaryContainerColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = "Language", tint = TertiaryColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Language", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            Text("மொழி அமைப்புகள்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isEnglishSelected = settingsState?.language != "தமிழ்"
                        // English Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isEnglishSelected) PrimaryColor else Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isEnglishSelected) PrimaryColor else OutlineVariantColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateSettingsLanguage("English")
                                    Toast.makeText(context, "Language switched to English", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("English", color = if (isEnglishSelected) Color.White else OnSurfaceColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Default Interface", color = if (isEnglishSelected) Color.White.copy(alpha = 0.8f) else OnSurfaceVariantColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Tamil Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (!isEnglishSelected) PrimaryColor else Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (!isEnglishSelected) PrimaryColor else OutlineVariantColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateSettingsLanguage("தமிழ்")
                                    Toast.makeText(context, "மொழி தமிழுக்கு மாற்றப்பட்டது", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("தமிழ்", color = if (!isEnglishSelected) Color.White else OnSurfaceColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
                                Text("Tamil Interface", color = if (!isEnglishSelected) Color.White.copy(alpha = 0.8f) else OnSurfaceVariantColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // AI Configuration Section (Gemini & Groq API Credentials)
            var selectedAiService by remember { mutableStateOf(viewModel.getActiveAiService()) }
            var geminiInput by remember { mutableStateOf(viewModel.getGeminiApiKey()) }
            var groqInput by remember { mutableStateOf(viewModel.getGroqApiKey()) }

            var isGeminiEditing by remember { mutableStateOf(viewModel.getGeminiApiKey().isEmpty()) }
            var isGroqEditing by remember { mutableStateOf(viewModel.getGroqApiKey().isEmpty()) }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth().testTag("ai_settings_card")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryContainerColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI settings", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("TuitionOS AI Assistant Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            Text("செயற்கை நுண்ணறிவு அமைப்புகள்", fontSize = 12.sp, color = OnSurfaceVariantColor, fontFamily = FontFamily.Default)
                        }
                    }

                    Text("Active Assistant Model Service:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Gemini", "Groq").forEach { service ->
                            val isSelected = selectedAiService == service
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) PrimaryColor else SurfaceColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryColor else OutlineVariantColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedAiService = service
                                        viewModel.saveActiveAiService(service)
                                        Toast.makeText(context, "Active model set to $service", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = service,
                                    color = if (isSelected) Color.White else OnSurfaceColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = OutlineVariantColor)

                    // === Gemini API Key Panel ===
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini Icon",
                                    tint = if (selectedAiService == "Gemini") PrimaryColor else OutlineColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gemini API Key", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            }
                            
                            val hasKey = viewModel.getGeminiApiKey().isNotEmpty()
                            if (hasKey && !isGeminiEditing) {
                                Text("✓ Configured", fontSize = 12.sp, color = Color(0xFF137333), fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("Not Configured", fontSize = 12.sp, color = OnSurfaceVariantColor)
                            }
                        }

                        if (isGeminiEditing) {
                            OutlinedTextField(
                                value = geminiInput,
                                onValueChange = { geminiInput = it },
                                placeholder = { Text("AI Studio Gemini Key") },
                                label = { Text("Gemini API Key") },
                                modifier = Modifier.fillMaxWidth().testTag("gemini_key_input"),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = OutlineVariantColor
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (viewModel.getGeminiApiKey().isNotEmpty()) {
                                    TextButton(onClick = { isGeminiEditing = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Button(
                                    onClick = {
                                        if (geminiInput.isNotBlank()) {
                                            viewModel.saveGeminiApiKey(geminiInput)
                                            isGeminiEditing = false
                                            Toast.makeText(context, "Gemini Key Saved & Confirmed!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Key cannot be empty", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Text("Confirm Use", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(12.dp))
                                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "••••••••••••••••" + if (geminiInput.length > 4) geminiInput.takeLast(4) else "",
                                    fontSize = 13.sp,
                                    color = OnSurfaceColor
                                )
                                Row {
                                    IconButton(
                                        onClick = { isGeminiEditing = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.saveGeminiApiKey("")
                                            geminiInput = ""
                                            isGeminiEditing = true
                                            Toast.makeText(context, "Gemini Key Removed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // === Groq API Key Panel ===
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Groq Icon",
                                    tint = if (selectedAiService == "Groq") PrimaryColor else OutlineColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Groq API Key", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            }
                            
                            val hasKey = viewModel.getGroqApiKey().isNotEmpty()
                            if (hasKey && !isGroqEditing) {
                                Text("✓ Configured", fontSize = 12.sp, color = Color(0xFF137333), fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("Not Configured", fontSize = 12.sp, color = OnSurfaceVariantColor)
                            }
                        }

                        if (isGroqEditing) {
                            OutlinedTextField(
                                value = groqInput,
                                onValueChange = { groqInput = it },
                                placeholder = { Text("Groq Cloud Console Key") },
                                label = { Text("Groq API Key") },
                                modifier = Modifier.fillMaxWidth().testTag("groq_key_input"),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = OutlineVariantColor
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (viewModel.getGroqApiKey().isNotEmpty()) {
                                    TextButton(onClick = { isGroqEditing = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Button(
                                    onClick = {
                                        if (groqInput.isNotBlank()) {
                                            viewModel.saveGroqApiKey(groqInput)
                                            isGroqEditing = false
                                            Toast.makeText(context, "Groq Key Saved & Confirmed!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Key cannot be empty", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Text("Confirm Use", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(12.dp))
                                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "••••••••••••••••" + if (groqInput.length > 4) groqInput.takeLast(4) else "",
                                    fontSize = 13.sp,
                                    color = OnSurfaceColor
                                )
                                Row {
                                    IconButton(
                                        onClick = { isGroqEditing = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.saveGroqApiKey("")
                                            groqInput = ""
                                            isGroqEditing = true
                                            Toast.makeText(context, "Groq Key Removed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // === Data Export / Import Section ===
            val scope = rememberCoroutineScope()
            var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
            var showImportConfirmDialog by remember { mutableStateOf(false) }

            val exportLauncher = rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri: Uri? ->
                if (uri != null) {
                    scope.launch {
                        val manager = com.example.data.DataExportImportManager(context)
                        manager.exportToFile(uri)
                        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            val importLauncher = rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                if (uri != null) {
                    pendingImportUri = uri
                    showImportConfirmDialog = true
                }
            }

            // Import Confirmation Dialog
            if (showImportConfirmDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    },
                    title = { Text("Confirm Import / இறக்குமதி உறுதிசெய்", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
                    text = { Text("This will REPLACE all existing data (students, fees, attendance, batches). This action cannot be undone!\n\nஇது அனைத்து தரவையும் மாற்றும். இந்த செயலை மீட்க முடியாது!") },
                    confirmButton = {
                        Button(
                            onClick = {
                                pendingImportUri?.let { uri ->
                                    scope.launch {
                                        val manager = DataExportImportManager(context)
                                        val result = manager.importFromFile(uri)
                                        result.fold(
                                            onSuccess = { count ->
                                                Toast.makeText(context, "Imported $count records", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { e ->
                                                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }
                                showImportConfirmDialog = false
                                pendingImportUri = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) { Text("Yes, Replace All") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImportConfirmDialog = false
                            pendingImportUri = null
                        }) { Text("Cancel") }
                    }
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = "Data", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Data Backup & Restore", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                            Text("தரவு காப்பு மற்றும் மீட்பு", fontSize = 11.sp, color = OnSurfaceVariantColor)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("tuitionos_backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryColor)
                        ) {
                            Icon(Icons.Default.Upload, "Export", tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryColor)
                        ) {
                            Icon(Icons.Default.Download, "Import", tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Import", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // === Logout Section ===
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, StatusInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Sign out from both local and cloud
                        val localAuth = LocalAuthManager(context)
                        localAuth.signOut()
                        // Clear all local user data to prevent leakage between users
                        viewModel.clearAllData()
                        if (SupabaseConfig.isConfigured) {
                            val cloudAuth = CloudAuthManager()
                            scope.launch {
                                cloudAuth.signOut()
                            }
                        }
                        navController.navigate(Dest.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Organization Details Edit Dialog
    if (showOrgEditDialog) {
        var editOrgName by remember(settingsState) { mutableStateOf(settingsState?.orgName ?: "") }
        var editPhone by remember(settingsState) { mutableStateOf(settingsState?.contactPhone ?: "") }
        var editPlan by remember(settingsState) { mutableStateOf(settingsState?.planName ?: "") }
        val orgNameErr = V.requiredFieldError(editOrgName, "Organization name") ?: V.maxLengthError(editOrgName, V.MAX_ORG_NAME_LENGTH, "Organization name")
        val orgPhoneErr = V.phoneError(editPhone)
        val orgValid = orgNameErr == null && orgPhoneErr == null

        AlertDialog(
            onDismissRequest = { showOrgEditDialog = false },
            icon = { Icon(Icons.Default.Business, null, tint = PrimaryColor) },
            title = { Text("Edit Organization", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editOrgName, onValueChange = { editOrgName = it.take(V.MAX_ORG_NAME_LENGTH) }, label = { Text("Organization Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = orgNameErr != null, supportingText = orgNameErr?.let { { Text(it, color = ErrorColor) } })
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("Contact Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = orgPhoneErr != null, supportingText = orgPhoneErr?.let { { Text(it, color = ErrorColor) } })
                    OutlinedTextField(value = editPlan, onValueChange = { editPlan = it.take(V.MAX_PLAN_LENGTH) }, label = { Text("Plan Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateSettings(orgName = editOrgName, contactPhone = editPhone, planName = editPlan)
                    showOrgEditDialog = false
                    Toast.makeText(context, "Settings updated!", Toast.LENGTH_SHORT).show()
                }, enabled = orgValid) { Text("Save", color = PrimaryColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showOrgEditDialog = false }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }

    // UPI ID Edit Dialog
    if (showUpiEditDialog) {
        var editUpi by remember(settingsState) { mutableStateOf(settingsState?.upiId ?: "") }
        val upiErr = V.upiError(editUpi)

        AlertDialog(
            onDismissRequest = { showUpiEditDialog = false },
            icon = { Icon(Icons.Default.QrCodeScanner, null, tint = PrimaryColor) },
            title = { Text("Edit UPI ID", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editUpi,
                    onValueChange = { editUpi = it.take(V.MAX_UPI_LENGTH) },
                    label = { Text("UPI ID") },
                    placeholder = { Text("yourname@bank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = upiErr != null,
                    supportingText = upiErr?.let { { Text(it, color = ErrorColor) } }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateSettings(upiId = editUpi)
                    showUpiEditDialog = false
                    Toast.makeText(context, "UPI updated!", Toast.LENGTH_SHORT).show()
                }, enabled = upiErr == null) { Text("Save", color = PrimaryColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showUpiEditDialog = false }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }
}

// 11. LEADS / INQUIRY MANAGEMENT SCREEN
@Composable
fun LeadsScreen(navController: NavController, viewModel: TuitionViewModel) {
    val leadsState by viewModel.leads.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var leadToEdit by remember { mutableStateOf<LeadEntity?>(null) }
    var leadToDelete by remember { mutableStateOf<LeadEntity?>(null) }

    Scaffold(
        topBar = {
            DashboardTopBar("TuitionOS", onSettingsClick = { navController.navigate(Dest.SETTINGS) })
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = Dest.LEADS)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLeadDialog = true },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Lead")
            }
        }
    ) { innerPadding ->

    if (showAddLeadDialog) {
        AddLeadDialog(
            onDismiss = { showAddLeadDialog = false },
            onSave = { name, standard, phone, source ->
                viewModel.addLead(
                    inquirerName = name,
                    standard = standard,
                    source = source,
                    status = "NEW",
                    phone = phone
                )
                showAddLeadDialog = false
                Toast.makeText(context, "Lead added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Column {
                Text("Lead Management", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                Text(
                    text = "Track and convert admission inquiries.",
                    fontSize = 14.sp,
                    color = OnSurfaceVariantColor
                )
            }

            // Summary Metrics Cards (Total & Conversion Rate)
            val totalLeads = leadsState.size
            val admittedLeads = leadsState.count { it.status == "ADMITTED" }
            val conversionRate = if (totalLeads > 0) (admittedLeads * 100) / totalLeads else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Leads
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, StatusInactive),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = "Total Leads", tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TOTAL LEADS", fontSize = 11.sp, color = OnSurfaceVariantColor, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$totalLeads", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("விசாரணைகள்", fontSize = 11.sp, color = OnSurfaceVariantColor)
                    }
                }

                // Conversion Rate
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainerColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PieChart, contentDescription = "Conversion", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONVERSION", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$conversionRate%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$admittedLeads admitted", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search leads... (விசாரணைகள் தேடு)") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = OutlineVariantColor
                )
            )

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "NEW", "CONTACTED", "ADMITTED")
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) PrimaryColor else Color.White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryColor else OutlineVariantColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else OnSurfaceVariantColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Lead List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val filteredLeads = leadsState.filter { lead ->
                    val matchesSearch = lead.inquirerName.contains(searchQuery, ignoreCase = true) ||
                            lead.standard.contains(searchQuery, ignoreCase = true)
                    val matchesFilter = when (selectedFilter) {
                        "NEW" -> lead.status == "NEW"
                        "CONTACTED" -> lead.status == "CONTACTED"
                        "ADMITTED" -> lead.status == "ADMITTED"
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }

                items(filteredLeads) { lead ->
                    val statusBgColor = when (lead.status) {
                        "NEW" -> Color(0xFFEFF4FF)
                        "CONTACTED" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFE6F4EA)
                    }
                    val statusTextColor = when (lead.status) {
                        "NEW" -> PrimaryColor
                        "CONTACTED" -> Color(0xFF92400E)
                        else -> Color(0xFF137333)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, StatusInactive),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressAnimation(onClick = {})
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(lead.inquirerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.School, contentDescription = "school", tint = OnSurfaceVariantColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(lead.standard, fontSize = 13.sp, color = OnSurfaceVariantColor)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(statusBgColor, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = lead.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusTextColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { leadToEdit = lead }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, "Edit lead", tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { leadToDelete = lead }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, "Delete lead", tint = ErrorColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(UiSurface, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (lead.source) {
                                            "WhatsApp Inquiry" -> Icons.AutoMirrored.Filled.Chat
                                            "Walk-in" -> Icons.Default.Campaign
                                            else -> Icons.Default.Language
                                        },
                                        contentDescription = "source",
                                        tint = OnSurfaceVariantColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Source: ${lead.source}", fontSize = 13.sp, color = OnSurfaceVariantColor)
                                }
                            }

                            if (lead.status != "ADMITTED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${lead.phone.ifEmpty { "0000000000" }}"))
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Icon(imageVector = Icons.Default.Call, contentDescription = "call", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontSize = 12.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val msg = WhatsAppTemplates.leadFollowUp(lead.inquirerName)
                                                val waLink = WhatsAppTemplates.buildWaLink(lead.phone.ifEmpty { "0000000000" }, msg)
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waLink))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandWhatsapp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "whatsapp", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Convert to Student Button
                                Button(
                                    onClick = {
                                        viewModel.addStudent(
                                            fullName = lead.inquirerName,
                                            standard = lead.standard,
                                            parentName = "Parent of ${lead.inquirerName}",
                                            parentPhone = lead.phone.ifEmpty { "0000000000" },
                                            monthlyFee = 2000.0,
                                            batchName = "General",
                                            status = "Pending"
                                        )
                                        viewModel.updateLeadStatus(lead, "ADMITTED")
                                        Toast.makeText(context, "${lead.inquirerName} enrolled as student!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Convert", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Convert to Student", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { /* action */ },
                                    enabled = false,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = SurfaceVariantColor, disabledContentColor = OnSurfaceVariantColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text("Profile Created", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Lead Dialog
    if (leadToEdit != null) {
        EditLeadDialog(
            lead = leadToEdit!!,
            onDismiss = { leadToEdit = null },
            onSave = { name, standard, phone, source ->
                val updated = leadToEdit!!.copy(
                    inquirerName = name,
                    standard = standard,
                    phone = phone,
                    source = source
                )
                viewModel.updateLead(updated)
                leadToEdit = null
                Toast.makeText(context, "Lead updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Lead Confirmation
    if (leadToDelete != null) {
        AlertDialog(
            onDismissRequest = { leadToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = ErrorColor) },
            title = { Text("Delete Lead?", fontWeight = FontWeight.Bold) },
            text = { Text("Delete inquiry from ${leadToDelete!!.inquirerName}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLead(leadToDelete!!)
                    leadToDelete = null
                    Toast.makeText(context, "Lead deleted", Toast.LENGTH_SHORT).show()
                }) { Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { leadToDelete = null }) { Text("Cancel", color = OutlineColor) }
            }
        )
    }
}

@Composable
fun EditLeadDialog(
    lead: LeadEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, standard: String, phone: String, source: String) -> Unit
) {
    var name by remember(lead) { mutableStateOf(lead.inquirerName) }
    var standard by remember(lead) { mutableStateOf(lead.standard) }
    var phone by remember(lead) { mutableStateOf(lead.phone) }
    var source by remember(lead) { mutableStateOf(lead.source) }

    val nameErr = V.requiredFieldError(name, "Name") ?: V.maxLengthError(name, V.MAX_NAME_LENGTH, "Name")
    val phoneErr = V.phoneError(phone)
    val isValid = nameErr == null && phoneErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, null, tint = PrimaryColor) },
        title = { Text("Edit Lead", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(V.MAX_NAME_LENGTH) }, label = { Text("Inquirer Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = nameErr != null, supportingText = nameErr?.let { { Text(it, color = ErrorColor) } })
                OutlinedTextField(value = standard, onValueChange = { standard = it.take(V.MAX_STANDARD_LENGTH) }, label = { Text("Standard/Class") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), isError = phoneErr != null, supportingText = phoneErr?.let { { Text(it, color = ErrorColor) } })
                OutlinedTextField(value = source, onValueChange = { source = it.take(V.MAX_SOURCE_LENGTH) }, label = { Text("Source") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name.trim(), standard.trim(), phone.trim(), source.trim())
            }, enabled = isValid) { Text("Save", color = PrimaryColor, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text("Cancel", color = OutlineColor) }
        }
    )
}

// SHARED REUSABLE COMPONENTS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    title: String,
    notificationCount: Int = 0,
    onSettingsClick: (() -> Unit)? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.ui.components.AppLogo()
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 20.sp
                )
            }
        },
        navigationIcon = {
            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SecondaryColor
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = PrimaryColor
                    )
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(StatusError, CircleShape)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (notificationCount > 9) "9+" else "$notificationCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val activeRoute = if (currentRoute.isNotBlank()) currentRoute else navBackStackEntry?.destination?.route ?: ""

    NavigationBar(
        containerColor = SurfaceColor,
        modifier = Modifier.height(72.dp)
    ) {
        val navItems = listOf(
            NavTab("Home", Dest.DASHBOARD, Icons.Default.Dashboard, Icons.Outlined.Dashboard, "முகப்பு"),
            NavTab("Students", Dest.STUDENT_LIST, Icons.Default.Group, Icons.Outlined.Group, "மாணவர்கள்"),
            NavTab("Batches", Dest.CREATE_BATCH, Icons.Default.CalendarToday, Icons.Outlined.CalendarToday, "வகுப்புகள்"),
            NavTab("Fees", Dest.FEES_SCHEDULE, Icons.Default.Payments, Icons.Outlined.Payments, "கட்டணம்"),
            NavTab("Leads", Dest.LEADS, Icons.Default.PersonAdd, Icons.Outlined.PersonAdd, "விசாரணை")
        )

        navItems.forEach { item ->
            val isActive = activeRoute == item.route

            NavigationBarItem(
                selected = isActive,
                onClick = {
                    if (activeRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Dest.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Active indicator pill
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(
                                    color = if (isActive) SecondaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(if (isActive) 4.dp else 7.dp))
                        Icon(
                            imageVector = if (isActive) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = if (isActive) SecondaryColor else OnSurfaceVariantColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) SecondaryColor else OnSurfaceVariantColor
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SecondaryColor.copy(alpha = 0.16f),
                    selectedIconColor = SecondaryColor,
                    unselectedIconColor = OnSurfaceVariantColor,
                    selectedTextColor = SecondaryColor,
                    unselectedTextColor = OnSurfaceVariantColor
                )
            )
        }
    }
}

data class NavTab(
    val label: String,
    val route: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val tamilLabel: String
)

fun isDueWithinThreeDays(dueDateStr: String, outstanding: Double): Boolean {
    if (outstanding <= 0.0) return false
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dueDate = sdf.parse(dueDateStr) ?: return false
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val limit = java.util.Calendar.getInstance().apply {
            time = today.time
            add(java.util.Calendar.DAY_OF_YEAR, 3)
        }
        val dueCal = java.util.Calendar.getInstance().apply {
            time = dueDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        dueCal.timeInMillis <= limit.timeInMillis
    } catch (e: Exception) {
        false
    }
}

private fun parseTimeToMinutes(time: String): Int? {
    return try {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        hours * 60 + minutes
    } catch (e: Exception) {
        null
    }
}

private fun formatGraceWindow(startTime: String, graceMinutes: Int = 10): String {
    val startMinutes = parseTimeToMinutes(startTime) ?: return ""
    val endMinutes = startMinutes + graceMinutes
    val endHour = endMinutes / 60
    val endMin = endMinutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", endHour, endMin)
}

// 12. FLEXIBLE FEE SCHEDULE & COLLECTION MANAGER
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScheduleScreen(navController: NavController, viewModel: TuitionViewModel) {
    val studentsState by viewModel.students.collectAsState()
    val feeHistoryState by viewModel.feeHistory.collectAsState()
    val attendanceRecordsState by viewModel.attendanceRecords.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val context = LocalContext.current
    
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditBalanceDialog by remember { mutableStateOf<FeeHistoryEntity?>(null) }
    
    // Add dialog state variables
    var selectedStudentName by remember { mutableStateOf("") }
    var inputMonth by remember { mutableStateOf("June 2026") }
    var inputInstallment by remember { mutableStateOf("Term 1 Tuition") }
    var inputAmount by remember { mutableStateOf("2500") }
    var inputDueDate by remember { mutableStateOf("2026-06-15") }
    var inputOutstanding by remember { mutableStateOf("2500") }
    var inputStatus by remember { mutableStateOf("Pending") }
    
    // Edit dialog state variables
    var editOutstandingAmount by remember { mutableStateOf("") }
    val monthErr = V.requiredFieldError(inputMonth, "Month") ?: V.maxLengthError(inputMonth, V.MAX_MONTH_LENGTH, "Month")
    val installmentErr = V.requiredFieldError(inputInstallment, "Installment") ?: V.maxLengthError(inputInstallment, V.MAX_INSTALLMENT_LENGTH, "Installment")
    val amountErr = V.amountError(inputAmount)
    val dueDateErr = V.dateError(inputDueDate)
    val outstandingErr = V.amountError(inputOutstanding)
    val feeFormValid = monthErr == null && installmentErr == null && amountErr == null && dueDateErr == null && outstandingErr == null

    val filteredFees = remember(feeHistoryState, selectedFilter) {
        if (selectedFilter == "All") {
            feeHistoryState
        } else {
            feeHistoryState.filter { it.status.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceColor)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Fee Schedules",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = "கட்டண அட்டவணை",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantColor
                        )
                    }
                    IconButton(
                        onClick = {
                            exportTuitionDataToCsv(context, studentsState, attendanceRecordsState, feeHistoryState)
                        },
                        modifier = Modifier
                            .background(PrimaryColor.copy(alpha = 0.1f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            tint = PrimaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(color = StatusInactive, thickness = 1.dp)
            }
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = Dest.FEES_SCHEDULE)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (studentsState.isNotEmpty()) {
                        selectedStudentName = studentsState.first().fullName
                        inputAmount = "2000"
                        inputOutstanding = "2000"
                        showAddDialog = true 
                    } else {
                        Toast.makeText(context, "Please enlist students first", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_fee_schedule_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UiSurface)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Section Info Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Fee Statements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
                Box(
                    modifier = Modifier
                        .background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredFees.size} RECORDED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }

            // Quick Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Paid", "Pending", "Overdue")
                filters.forEach { f ->
                    val isSelected = selectedFilter == f
                    val count = if (f == "All") feeHistoryState.size else feeHistoryState.count { it.status.equals(f, ignoreCase = true) }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) PrimaryColor else Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(BorderStroke(1.dp, if (isSelected) PrimaryColor else StatusInactive), RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = f }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = f,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else OnSurfaceColor
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) Color.White.copy(alpha = 0.2f) else SurfaceContainerHighest,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OnSurfaceVariantColor
                                )
                            }
                        }
                    }
                }
            }

            // Empty state check
            if (filteredFees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "No Schedules",
                            tint = OutlineColor,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No fee schedules match this filter.",
                            fontSize = 14.sp,
                            color = OnSurfaceVariantColor
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFees.size) { index ->
                        val fee = filteredFees[index]
                        val studentObj = studentsState.find { it.fullName == fee.studentName }
                        val standardLabel = studentObj?.standard ?: "Victory Academy Student"
                        val phoneNum = studentObj?.parentPhone ?: "+91 98765 43210"
                        
                        val isCritical = isDueWithinThreeDays(fee.dueDate, fee.outstandingBalance)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                width = if (isCritical) 1.5.dp else 1.dp,
                                color = if (isCritical) Color(0xFFE65100) else StatusInactive
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("fee_card_${fee.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fee.studentName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceColor
                                        )
                                        Text(
                                            text = standardLabel,
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariantColor
                                        )
                                    }

                                    val statusColor = when (fee.status.lowercase()) {
                                        "paid" -> StatusSuccess
                                        "overdue" -> ErrorColor
                                        else -> StatusWarning
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isCritical) {
                                            Row(
                                                modifier = Modifier
                                                    .background(Color(0xFFE65100).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                    .border(BorderStroke(1.dp, Color(0xFFE65100)), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Fee due soon alert",
                                                    tint = Color(0xFFE65100),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "DUE SOON",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = fee.status.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = StatusInactive.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Installment details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Installment Period & Title", fontSize = 10.sp, color = OnSurfaceVariantColor)
                                        Text("${fee.month} • ${fee.installment}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Billed Amount", fontSize = 10.sp, color = OnSurfaceVariantColor)
                                        Text("₹${String.format("%,.0f", fee.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dynamic outstanding/dues details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Scheduled Due Date", fontSize = 10.sp, color = OnSurfaceVariantColor)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday, 
                                                contentDescription = "Due Date", 
                                                tint = if (fee.status.lowercase() == "paid") OnSurfaceVariantColor else ErrorColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = fee.dueDate, 
                                                fontSize = 12.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = if (fee.status.lowercase() == "paid") OnSurfaceColor else ErrorColor
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Outstanding Balance", fontSize = 10.sp, color = OnSurfaceVariantColor)
                                        Text(
                                            text = "₹${String.format("%,.0f", fee.outstandingBalance)}", 
                                            fontSize = 14.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            color = if (fee.outstandingBalance > 0) ErrorColor else StatusSuccess
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Dynamic action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (fee.status.lowercase() != "paid") {
                                        Button(
                                            onClick = {
                                                val updated = fee.copy(status = "Paid", outstandingBalance = 0.0)
                                                viewModel.updateFeeHistory(updated)
                                                Toast.makeText(context, "Marked installment as Fully Paid!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            editOutstandingAmount = fee.outstandingBalance.toInt().toString()
                                            showEditBalanceDialog = fee
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                                        border = BorderStroke(1.dp, PrimaryColor),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Edit Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = {
                                            try {
                                                val encodedMsg = if (fee.status.lowercase() == "paid") {
                                                    WhatsAppTemplates.feePaidConfirmation(fee.studentName, fee.month, fee.amount)
                                                } else {
                                                    WhatsAppTemplates.feeReminder(fee.studentName, fee.amount, fee.dueDate, fee.outstandingBalance)
                                                }
                                                val waLink = WhatsAppTemplates.buildWaLink(phoneNum, encodedMsg)
                                                val waIntent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(waLink) }
                                                context.startActivity(waIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .background(BrandWhatsapp.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat, 
                                            contentDescription = "Send Reminder", 
                                            tint = BrandWhatsapp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (fee.status.lowercase() == "paid") {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    ReceiptGenerator.generateAndShare(
                                                        context = context,
                                                        data = ReceiptGenerator.ReceiptData(
                                                            orgName = settingsState?.orgName ?: "Tuition Center",
                                                            studentName = fee.studentName,
                                                            standard = standardLabel,
                                                            month = fee.month,
                                                            installment = fee.installment,
                                                            amount = fee.amount,
                                                            paidDate = fee.dueDate
                                                        )
                                                    )
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Receipt failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .background(PrimaryColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = "Share Receipt",
                                                tint = PrimaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to CREATE a brand new installment fee schedule
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Bill New Fee Schedule", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select enrolled student:", fontSize = 12.sp, color = OnSurfaceVariantColor)
                    
                    var studentExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerHighest, RoundedCornerShape(8.dp))
                            .border(1.dp, StatusInactive, RoundedCornerShape(8.dp))
                            .clickable { studentExpanded = !studentExpanded }
                            .padding(12.dp)
                    ) {
                        Text(selectedStudentName, fontSize = 14.sp)
                    }

                    if (studentExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .border(1.dp, StatusInactive, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            studentsState.forEach { st ->
                                Text(
                                    text = st.fullName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedStudentName = st.fullName
                                            studentExpanded = false
                                        }
                                        .padding(10.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputMonth,
                        onValueChange = { inputMonth = it.take(V.MAX_MONTH_LENGTH) },
                        label = { Text("Billing Month/Period") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = monthErr != null,
                        supportingText = monthErr?.let { { Text(it, color = ErrorColor) } }
                    )

                    OutlinedTextField(
                        value = inputInstallment,
                        onValueChange = { inputInstallment = it.take(V.MAX_INSTALLMENT_LENGTH) },
                        label = { Text("Installment Title (e.g. Term 1)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = installmentErr != null,
                        supportingText = installmentErr?.let { { Text(it, color = ErrorColor) } }
                    )

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it.take(V.MAX_AMOUNT_LENGTH) },
                        label = { Text("Total Amount Billed (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = amountErr != null,
                        supportingText = amountErr?.let { { Text(it, color = ErrorColor) } }
                    )

                    OutlinedTextField(
                        value = inputDueDate,
                        onValueChange = { inputDueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = dueDateErr != null,
                        supportingText = dueDateErr?.let { { Text(it, color = ErrorColor) } }
                    )

                    OutlinedTextField(
                        value = inputOutstanding,
                        onValueChange = { inputOutstanding = it.take(V.MAX_AMOUNT_LENGTH) },
                        label = { Text("Outstanding Balance (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = outstandingErr != null,
                        supportingText = outstandingErr?.let { { Text(it, color = ErrorColor) } }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statuses = listOf("Pending", "Paid", "Overdue")
                        statuses.forEach { s ->
                            val active = inputStatus == s
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (active) PrimaryColor else SurfaceContainerHighest, RoundedCornerShape(8.dp))
                                    .clickable { inputStatus = s }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else OnSurfaceColor)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val baseAmount = inputAmount.toDoubleOrNull() ?: 2000.0
                        val outstandingVal = inputOutstanding.toDoubleOrNull() ?: baseAmount
                        
                        viewModel.addFeeHistory(
                            studentName = selectedStudentName,
                            month = inputMonth,
                            installment = inputInstallment,
                            amount = baseAmount,
                            status = inputStatus,
                            dueDate = inputDueDate,
                            outstandingBalance = outstandingVal
                        )
                        showAddDialog = false
                    },
                    enabled = feeFormValid,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Add Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialogue to edit OUTSTANDING BALANCE for partial installments paid
    showEditBalanceDialog?.let { fee ->
        AlertDialog(
            onDismissRequest = { showEditBalanceDialog = null },
            title = { Text("Set Outstanding Balance", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update due balance for ${fee.studentName}'s installment (${fee.installment}).", fontSize = 13.sp, color = OnSurfaceVariantColor)
                    
                    val editOutstandingErr = V.amountError(editOutstandingAmount)
                    OutlinedTextField(
                        value = editOutstandingAmount,
                        onValueChange = { editOutstandingAmount = it.take(V.MAX_AMOUNT_LENGTH) },
                        label = { Text("Outstanding Balance (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = editOutstandingErr != null,
                        supportingText = editOutstandingErr?.let { { Text(it, color = ErrorColor) } }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBalance = editOutstandingAmount.toDoubleOrNull() ?: fee.outstandingBalance
                        val newStatus = if (newBalance <= 0) "Paid" else fee.status
                        val updated = fee.copy(
                            outstandingBalance = newBalance,
                            status = newStatus
                        )
                        viewModel.updateFeeHistory(updated)
                        showEditBalanceDialog = null
                    },
                    enabled = V.amountError(editOutstandingAmount) == null,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Save Balance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBalanceDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Add Lead Dialog with proper form fields
@Composable
fun AddLeadDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, standard: String, phone: String, source: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var standard by remember { mutableStateOf("10th Std") }
    var phone by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Walk-in") }

    val nameErr = V.requiredFieldError(name, "Inquirer name") ?: V.maxLengthError(name, V.MAX_NAME_LENGTH, "Name")
    val stdErr = V.maxLengthError(standard, V.MAX_STANDARD_LENGTH, "Class")
    val phoneErr = V.phoneRequiredError(phone)
    val isValid = nameErr == null && stdErr == null && phoneErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Lead / புதிய விசாரணை",
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(V.MAX_NAME_LENGTH) },
                    label = { Text("Inquirer Name (பெயர்) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = nameErr != null,
                    supportingText = nameErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = standard,
                    onValueChange = { standard = it.take(V.MAX_STANDARD_LENGTH) },
                    label = { Text("Standard / Class (வகுப்பு)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = stdErr != null,
                    supportingText = stdErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text("Phone Number (தொடர்பு எண்) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    isError = phoneErr != null,
                    supportingText = phoneErr?.let { { Text(it, color = ErrorColor) } },
                    singleLine = true
                )

                Text("Source / மூலம்:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sources = listOf("Walk-in", "WhatsApp", "Website", "Referral")
                    sources.forEach { s ->
                        val isSelected = source == s
                        Button(
                            onClick = { source = s },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryColor else Color.White,
                                contentColor = if (isSelected) Color.White else OnSurfaceVariantColor
                            ),
                            border = BorderStroke(1.dp, if (isSelected) PrimaryColor else StatusInactive),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(s, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.trim(), standard.trim(), phone.trim(), source)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Add Lead")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OutlineColor)
            }
        }
    )
}
