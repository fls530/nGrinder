import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import com.alibaba.fastjson.JSONObject
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@RunWith(GrinderRunner)
class LoginPost {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static String url = "http://192.168.85.128:8080/app/mobile/api/user/login"
    public static String mobile
    public static String body
    public static Cookie[] cookies = []
    // 存放参数文件记录
    public static lineList = List
    // 参数行
    public static def rowNumber


    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        test = new GTest(1, "login")
        request = new HTTPRequest()
        // Set header datas
        List<NVPair> headerList = new ArrayList<NVPair>()
        headerList.add(new NVPair("Content-Type", "application/json"))
        headers = headerList.toArray() as NVPair[]
        // 文本保存参数值
        lineList = new File("D:\\nGrinder\\script-sample\\test-with-login\\resources\\parm.txt").readLines()
        grinder.logger.info("before process.");
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports = true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");
    }

    @Test
    public void test() {
        // 获取文件的行数
        rowNumber = new Random().nextInt(lineList.size())
        // 获取文件中的数据
        mobile = lineList.get(rowNumber).toString()
        grinder.logger.info(mobile)
        // 对获取的值，进行接收，和jmeter里面一样，用${mobile}
        body = "{\"mobile\": \"${mobile}\", \"password\": \"123456\"}"
        grinder.logger.info(body)
        HTTPResponse result = request.POST(url, body.getBytes())
        //输出请求的返回结果信息
        grinder.logger.info(result.getText())
        //使用fastjson对json做反序列化
        JSONObject jsonObject = JSONObject.parseObject(result.getText())
        //获取请求只返回的code
        String token = jsonObject.getJSONObject("data").getString("token")
        System.println("token:" + token)
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
            //断言接口的返回码，建议用这个
            assertThat(result.text, containsString("\"code\":0"))
        }
    }
}
