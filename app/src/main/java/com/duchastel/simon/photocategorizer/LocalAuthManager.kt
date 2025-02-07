package com.duchastel.simon.photocategorizer

import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import com.duchastel.simon.photocategorizer.auth.AuthManager

val LocalAuthManager =
    compositionLocalWithComputedDefaultOf<AuthManager?> { null }