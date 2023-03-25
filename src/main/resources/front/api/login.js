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
        'url': '/chat',
        'method': 'post',
        params:{...data}
    })
}
//匿名私聊
function chatName2(data) {
    return $axios({
        'url': '/privateChat',
        'method': 'post',
        params:{...data}
    })
}
//匿名开房间
function chatName3(data) {
    return $axios({
        'url': '/other',
        'method': 'post',
        params:{...data}
    })
}

//查看群聊当天聊天记录
function chatSee() {
    return $axios({
        'url': '/chat',
        'method': 'get'
    })
}
//查看私聊当天聊天记录
function privateChatSee(data) {
    return $axios({
        'url': '/privateChat',
        'method': 'get',
        params:{...data}
    })
}

//查看开房间当天聊天记录
function roomChatSee(data) {
    return $axios({
        'url': '/other',
        'method': 'get',
        params:{...data}
    })
}

//查看本座地址
function addressSee() {
    return $axios({
        'url': '/addressSee',
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

  