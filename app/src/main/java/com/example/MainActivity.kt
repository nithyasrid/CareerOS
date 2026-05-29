package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    var activeTab by remember { mutableStateOf("Dashboard") } // Dashboard, Preparation, Programming, Expert, Placement

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CareerOS AI",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 20.sp
                        )
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "STUDIO PRO",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == "Dashboard",
                    onClick = { activeTab = "Dashboard" },
                    icon = { Icon(Icons.Default.School, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Preparation",
                    onClick = { activeTab = "Preparation" },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Prep") },
                    label = { Text("Prep Lab", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Programming",
                    onClick = { activeTab = "Programming" },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Program") },
                    label = { Text("Compiler", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Expert",
                    onClick = { activeTab = "Expert" },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Expert") },
                    label = { Text("Syllabus", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Placement",
                    onClick = { activeTab = "Placement" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Placement") },
                    label = { Text("Placement", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    "Dashboard" -> DashboardScreen()
                    "Preparation" -> PreparationScreen()
                    "Programming" -> ProgrammingScreen()
                    "Expert" -> ExpertScreen()
                    "Placement" -> PlacementScreen()
                    else -> DashboardScreen()
                }
            }
        }
    }
}
