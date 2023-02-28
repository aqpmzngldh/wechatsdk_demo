function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
}
// 短信接口
function sms(data) {
    return $axios({
        'url': '/sms',
        'method': 'post',
        data
    })
}

// 短信压力测试
function sss(data) {
    return $axios({
        'url': '/sss',
        'method': 'post',
        data
    })
}

//匿名群聊
function chatName(data) {
    return $axios({
        'url': '/chaat',
        'method': 'post',
        params:{...data}
    })
}

//查看群聊当天聊天记录
function chatSee() {
    return $axios({
        'url': '/chaat',
        'method': 'get'
    })
}

function sendMsgApi(data) {
    return $axios({
        'url': '/user/sendMsg',
        'method': 'post',
        data
    })
}

function loginoutApi() {
  return $axios({
    'url': '/user/loginout',
    'method': 'post',
  })
}

  