package com.example.hoof_care_02.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hoof_care_02.R
import com.example.hoof_care_02.model.Dog

enum class BottomNavTab { HOME, PETS, PROFILE }

@Composable
fun AppBottomNavigationBar(
    selectedTab: BottomNavTab,
    onHomeClick: () -> Unit,
    onPetsClick: () -> Unit,
    onProfileClick: () -> Unit,
    middleIsHealth: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.barranavegacao),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavItem(
                isActive = selectedTab == BottomNavTab.HOME,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.botaohome),
                    contentDescription = stringResource(R.string.nav_home)
                )
            }

            if (middleIsHealth) {
                NavItem(
                    isActive = selectedTab == BottomNavTab.PETS,
                    onClick = onPetsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.nav_health),
                        tint = if (selectedTab == BottomNavTab.PETS) Color(0xFF38C075) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                NavItem(
                    isActive = selectedTab == BottomNavTab.PETS,
                    onClick = onPetsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.botaomaiscachorro),
                        contentDescription = stringResource(R.string.nav_pets),
                        modifier = Modifier.height(85.dp)
                    )
                }
            }

            NavItem(
                isActive = selectedTab == BottomNavTab.PROFILE,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.botaoprofile),
                    contentDescription = stringResource(R.string.nav_profile)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38C075))
            )
        }
    }
}

@Composable
fun HeaderSection(userName: String, onProfileClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val photoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = stringResource(R.string.nav_profile),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { onProfileClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.fotousuario),
                    contentDescription = stringResource(R.string.nav_profile),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = stringResource(R.string.common_hello, userName), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Meus Pets", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DogCard(
    dog: Dog,
    onVerPerfil: () -> Unit,
    onSelecionar: () -> Unit,
    onExcluir: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF38C075))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = dog.photo ?: R.drawable.ic_launcher_background,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dog.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = dog.breed.name, color = Color.White, fontSize = 16.sp)
                    Text(text = stringResource(R.string.common_years, dog.age), color = Color.White, fontSize = 14.sp)
                }

                if (onExcluir != null) {
                    IconButton(
                        onClick = onExcluir,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.4f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onVerPerfil) {
                    Text("Ver Perfil", color = Color.White)
                }
                TextButton(onClick = onSelecionar) {
                    Text(stringResource(R.string.common_select), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HomeActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38C075)),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun HomeActionCard(
    text: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF38C075))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}