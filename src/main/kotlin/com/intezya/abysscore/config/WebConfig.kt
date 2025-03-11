package com.intezya.abysscore.config

import com.intezya.abysscore.security.interceptor.AccessLevelInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    @Autowired
    private lateinit var accessLevelInterceptor: AccessLevelInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(accessLevelInterceptor)
    }
}
