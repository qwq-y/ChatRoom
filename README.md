# 短对话群聊软件
汇报人：陈越航、汪清扬、段晟俊
指导老师：陈杉
时间：2023年 春季学期 第十八周

## 1. 背景与动机
一方面，我们目前对于安全协议的学习相对抽象，为了进一步理解密码学和相关协议的应用，我们希望学习和了解广为使用的端到端加密原理，并将其应用于实践，在具体的项目中使用此类协议来提高安全性。另一方面，考虑到小组成员对于项目开发均缺少经验，借此机会学习 web 的开发应用，有利于提高大家的开发和合作能力。因此，我们计划在 web 环境下，基于端到端加密原理，实现一个简单的短对话群聊软件。

## 2. 领域相关
Zoom 是一款被广泛使用的多人会议软件，支持使用端到端加密对会议进行保护。我们本次项目的主要参考对象即为 Zoom.
### 2.1 Zoom 的端到端加密
与非端到端加密相比，端到端加密将生成会议秘钥的职责从服务器端转移到了客户端。在端到端加密中，没有私钥信息或者未加密的信息会被提供给服务器。服务器无法知晓参与者交流的内容。
#### 2.1.1 系统组件
我们的端到端加密协议有以下组件：
* **身份管理系统**
用于管理用户账户、分发个人客户端生成的公钥。
* **信号通道**
用于分发参会者间的加密消息。
* **公告栏**
用于发放包含会议秘钥等的信息。
* **领导人**
对于一个群聊，始终有一位参会者扮演“领导人”的角色。其所在客户端负责生成并更新共享秘钥，并将这个秘钥以及其他元数据发送给其他参会者。如果领导人离开会议，则选择其他参会者作为领导人。
#### 2.1.2 加密算法
所有的数据都是经过 AES(GCM 模式) 加密后，使用 UDP 发送的。公钥加密与签名使用的是 Diffe-Hellman over Curve25519 和 EdDSA over Ed25519. 使用了 *libsodium* 库。

##### 2.1.2.1 签名
对于签名，直接使用了 *libsodium* 的 EdDSA 实现。
* **Sign.KeyGen** 生成一个密钥对 $(vk,sk)$
* **Sign.Sign** 输入为私钥 **sk**，上下文 **Context** 和消息 **Message**，输出为对 **SHA256(Context)||SHA256(M)** 的签名 **Sig**
* **Sign.Verify** 输入为公钥 **vk**，签名 **Sig**，上下文 **Context**，消息 **M**。如果验证成功则输出 true，失败则输出 false

##### 2.1.2.2 认证公钥加密
这部分依然使用 *libsodium*。
* **Box.KeyGen**
输入：无
输出：密钥对 $(pk_\text{Box},sk_\text{Box})$
* **Box.Enc**
输入：发送方私钥 $sk_{Box}^S$，接收方公钥 $pk_{Box}^R$，上下文 $\text{Context}_\text{KDF}$，上下文$\text{Context}_\text{Cipher}$，元数据 **Meta**，明文 **M**
输出：密文 **C**
* **Box.Dec**
输入：接收方私钥 $sk_{Box}^R$，发送方公钥 $pk_{Box}^S$，上下文 $\text{Context}_\text{KDF}$，上下文$\text{Context}_\text{Cipher}$，元数据 **Meta**，密文 **C**
输出：明文 **M**

#### 2.1.3 人员加入/离开时的协议流程
为了使用端到端加密，每个客户端需要一对设备签名秘钥。现将认证公钥表示为 $IVK$，签名私钥表示为 $ISK$. 设备的 $\text{hardwareID}$ 也会被用于协议中。
##### 生成参会者秘钥
当参会者 $i$ 加入会议时，不论 $i$ 是否是会议领导人，都会执行以下步骤：
1. 生成用于此次会议的临时密钥对 $(pk_i,sk_i)=\text{Box.KeyGen()}$
2. 获取本次会议的 $\text{meetingUUID}$
3. 计算 $\text{Binding}_i\leftarrow (\text{meetingID}||\text{meetingUUID}||i||\text{hardwareID}||IVK_i||pk_i)$
4. 定义 Context
5. 计算出签名 $\text{Sig}_i\leftarrow \text{Sign.Sign}(ISK_i,\text{Context, Binding}_i)$
6. 在会议期间保存好 $sk_i$
7. 将 $\text{Sig}_i$ 和 $pk_i$ 上传到公告栏，以使所有参会者可见

##### 领导人入会
1. 获取 $\text{meetingUUID}$
2. 使用安全随机数生成器生成 32-byte 会议秘钥 **MK** (每个 **MK** 会有与之关联的 **mkSeqNum**)
3. 获取参会人员列表 $I$
4. 对于每个参会者 $i \in I$，都做一遍“参会者入会（领导人端）”的子例程

##### 参会者入会（领导人端）
考虑领导人 $l$ 和参会者 $i$ 在设备 $\text{hardwareID}$ 上参加会议 $\text{meetingUUID}$，领导人需要：
1. 获取 $IVK_i$
2. 从会议的公告栏获取 $\text{Sig}_i$ 和 $pk_i$
3. 计算 $\text{Binding}_i\leftarrow (\text{meetingID}||\text{meetingUUID}||i||\text{hardwareID}||IVK_i||pk_i)$
4. 定义 $\text{Context}_\text{sign}$
5. 验证签名：$\text{Sign.Verify}(IVK_i,\text{Sig}_i,\text{Context}_\text{sign},\text{Binding}_i)$
6. 若验证失败则终止
7. 计算元数据 $\text{Meta}\leftarrow(\text{meetingID||meetingUUID||}l||i)$
8. 定义 $\text{Context}_\text{KDF}$
9. 定义 $\text{Context}_\text{cipher}$
10. 计算 $C\leftarrow\text{Box.Enc}(sk_l,pk_i,\text{Context}_\text{KDF},\text{Context}_\text{cipher},\text{Meta},(\text{MK,mkSeqNum}))$
11. 将 $(i,C)$ 上传到公告栏

##### 参会者入会（非领导人端）
基本与上述流程相反
1. 获取 $IVK_l$
2. 获取 $Sig_l,pk_l$
3. 获取 $(i,C_i)$
4. 获取 $\text{meetingUUID}$
5. 计算 $\text{Binding}_l\leftarrow (\text{meetingID}||\text{meetingUUID}||l||\text{hardwareID}||IVK_l||pk_l)$
6. 定义 $\text{Context}_\text{sign}$
7. 验证签名：$\text{Sign.Verify}(IVK_l,\text{Sig}_l,\text{Context}_\text{sign},\text{Binding}_l)$
8. 若验证失败则终止
9. 计算元数据 $\text{Meta}\leftarrow(\text{meetingID||meetingUUID||}l||i)$
10. 定义 $\text{Context}_\text{KDF}$
11. 定义 $\text{Context}_\text{cipher}$
12. 解密得到 $(\text{MK,mkSeqNum})$

至此，所有参会者都得到了会议秘钥 **MK**，并可以根据它加密、解密会议数据。

#### 2.1.4 Key Rotation
在会议中途，领导人可以生成新的会议秘钥 $\text{MK'}$,并更新至通知栏。发送 'rekey' 信号后，所有成员更新会议秘钥。同时需要确认 $\text{mkSeqNum}$ 比之前的值更大，否则忽略此次 key rotation.
每当有成员加入或离开会议，领导人都应该触发一次 key rotation.
每一次的会议秘钥是独立产生的，知道前一个会议秘钥并不会提供任何关于新的会议秘钥的信息。

#### 2.1.5 会议销毁
会议结束后或提前离开会议后，所有的参会者都应当丢弃该会议的会议秘钥及其衍生的秘钥，以及参会时产生的临时私钥 $sk_i$.
这样做的目的是提供前向安全。假设有攻击者可以记录下客户端之间传递的加密后的信息，并且可以从用户的设备上恢复出长期使用的秘钥，他仍然无法解出会议内容。

#### 2.1.6 用户标识与密钥管理
* 非加密用户标识
    一位用户通常会持有多个设备，并且在这些设备上都使用 Zoom 提供的服务。Zoom 的用户可以通过多种方式登陆到 Zoom，例如使用电子邮箱和对应密码，或者 OAuth，亦或是外部的 Identity Provider(IDP). 尽管有多种方式，电子邮箱仍然是用户的唯一标识符。除此以外，Zoom 会记录一些其他的有助于区别身份的信息，例如名字、职位（Zoom 主要面向企业）、公司、电话号码等。用户可以选择是否公开展示这些信息。
* 加密用户标识
    用户的加密用户标识分为两部分，分别是 1）为每个用户及其账户设置唯一的人类可读（便于人类阅读）的标识符；2）一组被用户控制的设备，及其密钥。Zoom 描述了一个设备模型，它可以解释用户的设备和密钥如何随着事件变化，并且可以解释设备之间的信赖关系。用户可以添加、删除设备（考虑设备弃用/丢失等的情况）。
#### 2.1.7 Signed hashchain(sigchain)
为了追踪用户标识随时间的变化（目的是使得它们可以被审查），signed hashchain 被引入 Zoom。Sigchain 是一个语句序列（或称为 links），每一个 link 包含前一个 link 的散列。在 Zoom 中，有着不同类型的 sigchain. 以 user sigchain 为例，它包含一个现阶段活跃的用户设备列表、一个已删除的设备列表、一个电子邮件列表、账户列表。Sigchain 的合法操作只能是增加，而不能是修改或者删除。因为做过的变更是无法被“遗忘”的。
* 一个记录修改电子邮件地址的 link 如下：
![](https://md.cra.moe/uploads/upload_080103d92edd95d8f0c1d3e39196320f.png)

#### 2.1.8 Per user keys(PUKs)
为了支持多设备，Zoom 引入了 Per user key 机制。Per user key 是在用户所有设备之间共享的一组密钥。它会在有新设备加入，或者有设备被删除时进行 key rotation.


## 3. 项目预期
* **协议理解**
参考 Zoom 协议的白皮书及其他相关资料，学习端到端加密原理在群聊中的使用，理解端到端加密和非端到端加密的区别和优缺点。考虑密钥迭代、用户加入和退出、聊天记录保存、主持人和公告板机制等的作用，讨论常见的攻击形式，尽可能完整地理解群聊中端到端加密的应用。

* **软件功能**
1. 加入和退出群聊室：
群聊发起者创建聊天室，其他群聊者可以根据聊天室编号进入聊天室。进入和退出聊天室的过程应当包含密钥的创建、迭代、分发、验证。
3. 用户登录：
用户进入聊天室时应当进行登录和身份认证，身份认证成功后可加入聊天室。
4. 发送和接收信息：
用户可以在聊天室中发送文字信息，同时处于同一聊天室的用户均可以接收到信息。信息的发送和接收应当使用端到端加密原理，信息对服务器保密。

## 4. 当前进展
### 第七周 
提出了大致的项目计划和成员分工，并查找了主要资料，处于学习过程中。
### 第十二周
#### 4.1 协议理解
对端到端加密协议有了更进一步的理解，主要参见 2.1.5-2.1.8。
#### 4.2 项目后端
一方面，为了熟悉网络通信，我们使用较为简单的 Socket 和 JavaFX 实现了一个可以在本地运行的在线聊天室，对于项目在 server 和 client 端的实现有了大致规划，并且熟悉了 socket 的使用。
源码地址：https://github.com/qwq-y/OnlineChatRoom_Socket_JavaFX
另一方面，我们学习了基础的项目开发技术，建立了一个基于 SpringBoot 架构的后端项目，对架构的开发使用有了总体了解，并且在其中实现了简单的功能。目前，主要实现了对 SpringBoot, Spring MVC, Mybatis 的整合，并连接到数据库。使用 Postman 进行测试，可以对 http 请求进行响应，返回数据库查询内容。
#### 4.3 项目前端
经过学习，目前前端开发部分可以实现基本的按钮、聊天窗等功能，能实现本地的多窗口聊天，不过系统的开发和测试还有待完善。相关代码部分已上传github，后续还会随着学习进度继续更新版本。

### 第十八周
#### 4.4 项目实现情况
经过对端到端加密以及非对称和对称结合形式的加密的学习，我们基于一个简单聊天室的demo完成了更进一步功能以及消息加密签名的实现。

项目源码地址：https://github.com/FrankDuanCool/Easy-Chat
#### 4.4.1 加密方式
每个聊天室都有一个**leader**，每个进入聊天室的用户都会在进入时使用**jsrsasign**库，以**RSA**加密方式生成自己独有的公钥和私钥，以及用**tweetnacl**库生成一个**EdDSA**加密用的公钥私钥对，并把这两个公钥上传到服务器，服务器将用户列表以及公钥信息转发给所有用户。每当聊天室内发生人员变动，**leader**就会随机生成一个**32**位的会议密钥，并把这个密钥用每一个参与聊天的用户提供的自己的**RSA**公钥加密会议密钥，然后把这个加密后的会议密钥分发给对应的用户。每一个用户收到被加密过的会议密钥后，用自己的私钥解密得到会议密钥的内容并更新会议密钥。在此之后，如果成员**A**希望发送消息给成员**B**，那么会使用**CryptoJS**库的**AES**方法用会议密钥加密，用**EdDSA**私钥给信息签名，并发送给对方。**B**收到加密信息后，先用自己有的对方的**EdDSA**公钥信息验证签名是否正确，然后用**CryptoJS**库的**AES**方法用会议密钥进行解密，得到**A**想发送给**B**的信息。在上述过程中，服务器只会保存参会者的名字，以及他们所提供的加密会议密钥用的公钥信息和签名验证用的公钥信息。这样一来，攻击者从服务器无法得知除了信息来源和去向以外的任何信息，对信息的任何修改操作也会被用户发现并拒绝接受消息。通过这种方式，结合非对称加密和对称加密实现了较为安全的端到端加密。

值得一提的是，既然使用的是**http**，这意味着用户这边也容易被攻击。本项目的端到端实现逻辑仅是我们理解协议相关内容后做出的模拟实现，并不能做到绝对安全。
#### 4.4.2 项目内容演示
##### 登陆

![chatview](/imgs/chatview.png)

##### 注册

![register](/imgs/register.png)

##### 聊天

![a](/imgs/a.png)

![b](/imgs/b.png)

![c](/imgs/c.png)

支持私聊，群聊功能。上图显示了三个用户的聊天界面，由于加入聊天与发送信息的时间先后（后加入的用户看不到过去的消息），各个用户显示的消息不一样。

## 5. 收获与反思
本次创新实践中，我们比上一次创新实践有了较大的进步，做出的结果已可以有实际应用。在本次创新实践项目中，我们对于Web开发、SpringBoot开发都有了更深入的了解，同时也锻炼了团队协作的能力。我们通过这一次对Web聊天室的开发实践进一步理解了协议的内容以及亲自模拟了加密签名等操作的具体流程，但是我们已经了解到，在网页上实现端到端加密并不能真正保护数据安全，因为其依赖于浏览器，并且代码也极易被篡改。

## 6. 参考文献
[Zoom end to end whitepaper](https://github.com/zoom/zoom-e2e-whitepaper)

## 7. 成员分工
段晟俊主要负责实现前端开发加密签名算法部分，陈越航主要负责前端开发工作以及协议理解指导，汪清扬主要负责后端开发。



