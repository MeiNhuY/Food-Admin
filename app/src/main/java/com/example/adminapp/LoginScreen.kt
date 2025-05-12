package com.example.adminapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.adminapp.Domain.UserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (UserModel) -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
    ) {
        ConstraintLayout(
            modifier = Modifier
                .height(350.dp)
                .offset(y = (-20).dp)
                .fillMaxWidth()
        ) {
            val (backgroundImg, logImg, loginText) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.intro_pic),
                contentDescription = null,
                modifier = Modifier
                    .height(900.dp)
                    .offset(x = 4.dp, y = 50.dp)
                    .constrainAs(backgroundImg) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .offset(y = (-80).dp),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(R.drawable.pizza),
                contentDescription = null,
                modifier = Modifier
                    .height(300.dp)
                    .offset(x = 5.dp, y = (-10).dp)
                    .constrainAs(logImg) {
                        top.linkTo(backgroundImg.top)
                        bottom.linkTo(backgroundImg.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentScale = ContentScale.Fit
            )

            // Viền chữ
            Text(
                text = "Đăng Nhập",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    drawStyle = Stroke(width = 10f)
                ),
                modifier = Modifier.constrainAs(loginText) {
                    top.linkTo(logImg.bottom, margin = (-50).dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Lớp trong chữ
            Text(
                text = "Đăng Nhập",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA500)
                ),
                modifier = Modifier.constrainAs(loginText) {
                    top.linkTo(logImg.bottom, margin = (-50).dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CTextField(hint = "Email", value = email, onValueChanged = { email = it })
        CTextField(
            hint = "Password",
            value = password,
            onValueChanged = { password = it },
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            val repository = remember { MainRepository() }

            CButton(text = "Đăng Nhập", onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    repository.loginUser(
                        email, password,
                        onSuccess = { user ->
                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(user)
                        },
                        onFailure = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CTextField(
    onValueChanged: (String) -> Unit = {},
    hint: String,
    value: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChanged,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        placeholder = {
            Text(
                text = hint,
                style = TextStyle(
                    fontSize = 22.sp,
                    color = Color(0xFFBEC2C2)
                )
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun CButton(
    onClick: () -> Unit = {},
    text: String,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7C9A92)
        ),
        modifier = Modifier
            .width(250.dp)
            .height(52.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        )
    }
}
