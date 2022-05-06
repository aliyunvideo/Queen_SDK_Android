/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.lib_live_pk_service.bean

import com.google.gson.Gson
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment

data class PkEndInfo(
    val type: Int,
    val pkStartTime: Long,//	PK 开始时间
    val pkEndTime: Long,//	PK 结束时间
    val reason: Int,//1 normal 2 abnormal
    val inviterRewards: Long,//	邀请者打赏总额
    val inviteeRewards: Long,//	被邀请者打赏总额
    val countDownEnd: Boolean//	是否计时结束
) : MsgAttachment {
    /**
     * 将消息附件序列化为字符串，存储到消息数据库或发送到服务器。<br></br>
     * @param send 如果你的附件在本地需要存储额外数据，而这些数据不需要发送到服务器，可依据该参数值区别对待。
     * @return
     */
    override fun toJson(send: Boolean): String {
        return Gson().toJson(this)
    }

}