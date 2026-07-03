package com.example.hoof_care_02.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.hoof_care_02.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.ui.theme.HoofBlack
import com.example.hoof_care_02.ui.theme.HoofDivider
import com.example.hoof_care_02.ui.theme.HoofFieldBorder
import com.example.hoof_care_02.ui.theme.HoofWhite


@Composable
fun RoundedAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = HoofBlack.copy(alpha = 0.6f)) },
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        enabled = enabled,
        isError = isError,
        supportingText = if (supportingText != null) {
            { Text(supportingText, color = Color(0xFFD32F2F)) }
        } else null,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HoofWhite,
            unfocusedContainerColor = HoofWhite,
            disabledContainerColor = HoofWhite.copy(alpha = 0.6f),
            focusedBorderColor = HoofFieldBorder,
            unfocusedBorderColor = HoofFieldBorder,
            focusedTextColor = HoofBlack,
            unfocusedTextColor = HoofBlack
        ),
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun PrimaryAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = HoofBlack, 
            contentColor = HoofWhite,
            disabledContainerColor = HoofBlack.copy(alpha = 0.6f),
            disabledContentColor = HoofWhite.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}


@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(HoofDivider)
        )
        Text(
            text = stringResource(R.string.auth_or_divider),
            color = HoofWhite,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(HoofDivider)
        )
    }
}

@Composable
fun SocialAuthButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = HoofWhite, 
            contentColor = HoofBlack,
            disabledContainerColor = HoofWhite.copy(alpha = 0.6f),
            disabledContentColor = HoofBlack.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.dp, HoofWhite),
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .height(20.dp)
                .padding(end = 8.dp)
        )
        Text(text)
    }
}
