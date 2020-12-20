import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Date
import java.util.List
import java.util.ArrayList

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair

/**
 * A simple example using the HTTP plugin that shows the retrieval of a
 * single page via HTTP.
 *
 * This script is automatically generated by ngrinder.
 *
 * @author admin
 */
// 每个测试类都要加这个注解
@RunWith(GrinderRunner)
class Get_type {

    public static GTest test
    // 定义 HTTPRequest 静态变量 request，用于发送 HTTP 请求
    public static HTTPRequest request
    // 定义 NVPair 数组 headers ，用于存放通用的请求头数据
    public static NVPair[] headers = []
    // 定义 NVPair 数组 params ，用于存放请求参数数据
    public static NVPair[] params = []
    // 定义 Cookie 数组 cookies ，用于存放通用的 cookie 数据
    public static Cookie[] cookies = []

    //每个进程启动前执行
    @BeforeProcess
    public static void beforeProcess() {
        // 加载资源文件，初始化GTest等
        // 设置请求响应超时时间（ms），超过则抛出异常
        HTTPPluginControl.getConnectionDefaults().timeout = 6000

        // 创建GTest对象，第一个参数1代表有多个请求/事务时的执行顺序ID
        // 第二个参数是请求/事务的名称，会显示在summary结果中
        // 有多个请求/事务时，要创建多个GTest对象
        test = new GTest(1, "get_type")
        // 创建 HTTPRequest 对象，用于发起 HTTP 请求
        request = new HTTPRequest()
        grinder.logger.info("before process.");
    }

    // 每个线程执行前执行
    @BeforeThread
    // 登录、设置cookie之类
    public void beforeThread() {
        // 注册事件，启动test，第二个参数要与@Test注解的方法名保持一致
        // 有多个请求/事务时，要注册多个事件
        test.record(this, "test")
        // 配置延迟报告统计结果
        grinder.statistics.delayReports=true;
        grinder.logger.info("before thread.");
    }

    // 在每个@Test注解的方法执行前执行
    @Before
    // 设置变量、咁多嘅@Test注解的方法执行前执行
    public void before() {
        // 在这里可以添加headers属性和cookies
        request.setHeaders(headers)
        // 设置本次请求的 cookies
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");
    }

    // 在测试结束前不断运行，各个@Test注解的方法异步执行
    @Test
    public void test(){
        // 发送GET请求
        HTTPResponse result = request.GET("http://192.168.85.128:8080/app/mobile/api/goods/gettypes?type=1", params)
        //输出请求的返回结果信息
        grinder.logger.info(result.getText())
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            //断言HTTP请求返回的状态码
            assertThat(result.statusCode, is(200));
            //断言接口的返回码，建议用这个
            assertThat(result.text, containsString("\"code\":0"))
        }
    }
}