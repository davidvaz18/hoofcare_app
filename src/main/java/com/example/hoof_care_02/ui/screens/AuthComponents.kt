package com.example.hoof_care_02.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.ui.theme.HoofBlack
import com.example.hoof_care_02.ui.theme.HoofDivider
import com.example.hoof_care_02.ui.theme.HoofFieldBorder
import com.example.hoof_care_02.ui.theme.HoofWhite

/**
 * Campo de texto com cantos arredondados, equivalente ao drawable/rounded_corner.xml
 * usado nos EditText originais das telas de Login e Cadastro.
 */
@Composable
fun RoundedAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = HoofBlack.copy(alpha = 0.6f)) },
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HoofWhite,
            unfocusedContainerColor = HoofWhite,
            focusedBorderColor = HoofFieldBorder,
            unfocusedBorderColor = HoofFieldBorder,
            focusedTextColor = HoofBlack,
            unfocusedTextColor = HoofBlack
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Botão preto "Continue" / "Continuar", igual aos botões com backgroundTint #000000. */
@Composable
fun PrimaryAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = HoofBlack, contentColor = HoofWhite),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

/** Divisor "Ou" entre os botões de login social, igual ao LinearLayout com duas Views + TextView. */
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
            text = "Ou",
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

/** Botão social branco com ícone à esquerda (Google / Facebook). */
@Composable
fun SocialAuthButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = HoofWhite, contentColor = HoofBlack),
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
