package com.intezya.abysscore

import com.intezya.abysscore.utils.containers.TestPostgresConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestPostgresConfiguration::class)
class AbyssCoreApplicationTests {
    //    @Test
//    fun contextLoads() {
//    }
}
