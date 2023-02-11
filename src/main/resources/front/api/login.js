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

  