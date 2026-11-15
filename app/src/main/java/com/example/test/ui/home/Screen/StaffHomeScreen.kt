package com.example.test.ui.home.Screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun StaffHomeScreen() {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Nhan vien", modifier = Modifier.padding(top = 12.dp))

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, text = {
                    Text("Thiet bi")
                }, onClick = { selectedTab = 0 })

                Tab(
                    selected = selectedTab == 1,
                    text = { Text("Phieu cua toi ") },
                    onClick = { selectedTab = 1 })
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> Text("Danh sach thiet bi se o day")
                    1 -> Text("Phieu muon cua ban se o day")
                }
            }
        }

    }
}

@Preview
@Composable
fun StaffHomePreview() {
    StaffHomeScreen()
}
