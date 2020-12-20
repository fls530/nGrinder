import com.alibaba.fastjson.JSONObject

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class LoginPlayTest2 {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static String url = "http://192.168.111.129:8080/app/mobile/api/user/login"
    public static String body = "{\"mobile\": \"15800000002\", \"password\": \"123456\"}"
    public static Cookie[] cookies = []
    public static String token
    public static String url_list = "http://192.168.111.129:8080/app/mobile/api/order/getorders"
    // 定义 NVPair 数组 params ，用于存放请求参数数据
    public static NVPair[] params = []
    public static GTest login
    public static GTest playList


    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        // 实例化两个GTest对象
        login = new GTest(1, "login")
        playList = new GTest(2,"playList")

        request = new HTTPRequest()
        // Set header datas
        List<NVPair> headerList = new ArrayList<NVPair>()
        headerList.add(new NVPair("Content-Type", "application/json"))
        headers = headerList.toArray()
        grinder.logger.info("before process.");
    }

    @BeforeThread
    public void beforeThread() {
        //设置两个请求的统计测试结果
        login.record(this, "login")
        playList.record(this,"playList")

        grinder.statistics.delayReports=true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");
    }

    @Test
    public void test(){
        //获取总虚拟用户和运行线程数
        int vusers = getVusers()
        int runThreadNum = getRunThreadNum()

        // 运行百分比例设置
        int runRate1 = 60
        int runRate2 = 40

        // 计算线程运行比例数
        int runNum1 = vusers/100 * runRate1
        int runNum2 = vusers/100 * (runRate1+runRate2)

        //根据比例进行相应请求
        if(runThreadNum > 0 && runThreadNum <=runNum1)
            this.login()
        else if(runThreadNum >runNum1 && runThreadNum <= runNum2)
            this.playList()
    }

    // 登录
    public void login() {
        // 发送登录请求
        HTTPResponse result = request.POST(url, body.getBytes())
        //输出请求的返回结果信息
        grinder.logger.info(result.getText())
        JSONObject jsonobj = JSONObject.parseObject(result.getText())
        // 获取请求返回的code 值
        int status = (int) jsonobj.getInteger("code")
        System.out.println("code：" +status)
        // 获取token值
        token = (String) jsonobj.getJSONObject("data").get("token")
        System.out.println("token：" +token)
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
            //断言接口的返回码，建议用这个
            assertThat(result.text, containsString("\"code\":0"))

        }
    }

    public void playList() {
        // 获取订单列表
        List<NVPair> paramList = new ArrayList<NVPair>()
        // 拼装请求参数
        paramList.add(new NVPair("offset", "0"))
        // 获取登录返回的token
        paramList.add(new NVPair("token", token))
        params = paramList.toArray()
        HTTPResponse result1 = request.GET(url_list, params)
        JSONObject jsonobj1 = JSONObject.parseObject(result1.getText())
        int status = (int) jsonobj1.getInteger("code")
        if(status==0){
            assertThat(result1.text, containsString("\"code\":0"))
        }else
        {
            grinder.logger.error("失败了", status)
        }
        //输出请求的返回结果信息
        grinder.logger.info(result1.getText())


    }
    public int getVusers(){
        // 获取虚拟用户总数
        int totalAgents = Integer.parseInt(grinder.getProperties().get("grinder.agents").toString())
        int totalProcesses = Integer.parseInt(grinder.properties.get("grinder.processes").toString())
        int totalThreads = Integer.parseInt(grinder.properties.get("grinder.threads").toString())
        int vusers = totalAgents * totalProcesses * totalThreads
        return vusers

    }

    public int getRunThreadNum() {
        // 获取当前压力机器数、进程数、线程数
        int agentNum = grinder.agentNumber
        int processNum = grinder.processNumber
        int theadNum = grinder.threadNumber

        // 获取当前线程数
        int runThreadNum = (agentNum + 1) * (processNum+1) * (theadNum+1)
        return runThreadNum
    }
}
