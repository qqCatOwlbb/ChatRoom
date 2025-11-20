好的，这是一个非常具有挑战性的升级！

将 `spring-boot-starter-websocket` 替换为 **Netty** 是一个“专家级”的 Bonus 选项。这表明你不想只停留在 Spring 封装好的框架上，而是想深入底层，自己掌控网络通信、TCP/IP、协议编解码和高性能 I/O 模型。

这会**显著增加项目的复杂性**，尤其是在**安全集成**和**Spring Bean 管理**方面，但你将获得对性能和连接的极致控制。

数据库设计与我上次提供的**完全相同**，你仍然需要所有这些表。

这是为你更新的、集成了 Netty 的**新项目开发规划**：

------



### 🗺️ 新的项目学习与开发规划 (Netty 版)





#### 阶段一：项目搭建、用户与视频基础 (约 20%)



**目标：** 搭建一个能让用户登录、上传和观看（已批准）视频的基础网站。**这一步至关重要**，因为你需要一个能颁发“身份凭证”（如 JWT Token）的系统，以便后续在 Netty 中进行身份验证。

1. **项目初始化与 Spring Security：**
   - 创建 Spring Boot 项目，引入 Web, Security, Data-JPA (或 MyBatis), MySQL 依赖。
   - 实现**登录/注册**功能，使用 `BCryptPasswordEncoder` 加密密码。
   - 实现 `UserDetailsService` 从 `user_account` 表加载用户。
   - **重点：** 配置 Spring Security，使其在用户登录成功后，返回一个**JWT (JSON Web Token)**。这个 Token 将是你的 Netty 服务器验证用户身份的唯一凭据。
2. **视频基础功能：**
   - 实现视频**上传** API，受 Spring Security 保护（需要 JWT）。视频信息写入 `video` 表，`status` 为 `'pending'`。
   - 实现视频**观看** API（可公开），但只返回 `status` = `'approved'` 的视频。



#### 阶段二：Netty WebSocket 服务器与安全集成 (约 35%)



**目标：** 用 Netty 手动实现一个 WebSocket 服务器，并将其与 Spring 安全体系打通。这是整个项目的**最难点**。

1. **学习 Netty 基础：**
   - **理论：** 理解 Netty 的核心概念：`EventLoopGroup` (Boss/Worker 线程池), `ServerBootstrap`, `Channel`, `ChannelPipeline`, `ChannelHandler`。
   - **实践：** 引入 `netty-all` 依赖。编写一个最简单的 Netty "Hello World" 服务器。
2. **实现 WebSocket 协议：**
   - 在 Netty 的 `ChannelPipeline` 中，添加 Netty 提供的处理器（Handler）来支持 HTTP 协议，并处理 WebSocket 握手：
     - `HttpServerCodec`：HTTP 编解码器。
     - `HttpObjectAggregator`：将 HTTP 消息聚合为完整的 FullHttpRequest。
     - `WebSocketServerProtocolHandler`：**Netty 的核心**，用于处理 WebSocket 握手、Ping/Pong 帧和协议升级。你需要配置它的 `websocketPath` (例如 `/ws`)。
3. **安全集成（重点）：**
   - `spring-boot-starter-websocket` 能自动获取 Spring Security 的上下文，**Netty 不能**。
   - 你必须在 WebSocket 握手（HTTP 升级）时**手动鉴权**。
   - **方案：** 客户端在发起 WebSocket 连接时，必须携带 Token。例如：`ws://your.domain.com/ws?token=...JWT...`
   - **实现：**
     1. 自定义一个 `ChannelInboundHandlerAdapter`，放在 `WebSocketServerProtocolHandler` **之前**。
     2. 在此 Handler 中，捕获 `FullHttpRequest`（握手请求）。
     3. 解析 URL，获取 `token` 参数。
     4. 调用你 Spring 容器中的 `JwtTokenUtil` (需要想办法将其注入到 Netty Handler 中) 来**验证 Token**。
     5. **如果验证失败：** 立即关闭 `Channel`，阻止握手。
     6. **如果验证成功：** 将解析出的 `userId` 存储在 `Channel` 的属性中（`ctx.channel().attr(USER_ID_KEY).set(userId);`），以便后续业务 Handler 使用。然后将请求传递给 `WebSocketServerProtocolHandler`。
4. **管理 Channel：**
   - 创建一个全局单例（或 Spring Bean）`ChannelRegistry`，使用 `ConcurrentHashMap<Long, Channel>` 来存储 `userId` 到 `Channel` 的映射。
   - 在你的业务 Handler 中，当 `channelActive` 事件触发且鉴权成功后，将 `(userId, channel)` 存入 Map；当 `channelInactive` 时，将其移除。



#### 阶段三：核心 IM 逻辑与业务模块 (约 20%)



**目标：** 在 Netty 服务器之上，构建 IM 的四大模块功能。

1. **自定义 WebSocket 业务 Handler：**
   - 创建另一个 `SimpleChannelInboundHandlerAdapter`，放在 `WebSocketServerProtocolHandler` **之后**，用于处理 `TextWebSocketFrame` (文本消息)。
   - **消息接收：**
     1. 当收到 `TextWebSocketFrame` 时，从 `ctx.channel().attr(USER_ID_KEY)` 中获取发送者 `senderUserId`。
     2. 解析消息的 JSON 字符串（例如 `{type: 'single', toUserId: '123', content: 'hello', messageUid: 'client-uuid-123'}`）。
     3. 将这个消息（连同 `senderUserId`）传递给你 Spring 容器中的 `@Service` (如 `MessageService`)。
2. **实现四大模块 (同步版)：**
   - **MessageService (同步)：**
     1. （为保证时序性）原子性地获取此会话的 `sequence_id`（来自 `conversation_sequence` 表）。
     2. （为保证幂等性）检查 `message_uid` 是否已存在，如果存在则丢弃。
     3. 将消息**同步写入** MySQL 的 `message` 表。
   - **消息转发：**
     1. `MessageService` 查找接收者 `toUserId`。
     2. 从 `ChannelRegistry` 中获取接收者的 `Channel`。
     3. 如果 `Channel` 存在且活跃（`isWritable()`），则将消息打包成 `TextWebSocketFrame`，通过 `recipientChannel.writeAndFlush(frame)` **直接发送**。
   - **联系人/会话模块：**
     - 这些模块（如好友列表、会话列表、历史记录）的逻辑与之前规划相同，**但它们是普通的 Spring `@RestController`**。
     - 前端通过**普通的 HTTP API** (不是 WebSocket) 来拉取这些信息。
     - 实现 `contact`, `conversation` 表的相关 CRUD API。



#### 阶段四：视频审核模块 (约 5%)



**目标：** 完成基于 Spring Security 的视频审核功能。

1. **实现 API：** 创建审核相关的 Controller（`/api/audit/...`）。
2. **安全：** 使用 `@PreAuthorize("hasRole('AUDITOR')")` 保护这些 API。
   - **注意：** 这一步非常简单，因为它**完全不涉及 Netty**，它走的是 Spring Boot 内嵌 Tomcat 的标准 HTTP 流程。



#### 阶段五：异步与缓存优化 (MQ & Redis) (约 15%)



**目标：** 将 IM 持久化操作改为异步，并加入 Redis 缓存。

1. **集成 RabbitMQ：**
   - 引入 `spring-boot-starter-amqp`。
   - **重构 `MessageService`：**
     1. 当收到消息时，**不再**同步写入 MySQL。
     2. 改为将消息（包含 `senderUserId`, `sequence_id`, `message_uid` 等）**发送到 RabbitMQ**。
     3. **消息转发逻辑不变：** 立即从 `ChannelRegistry` 查找并转发给在线用户。
2. **MQ 消费者与 Redis：**
   - 创建 `@RabbitListener` 消费者。
   - 消费者从队列取到消息，执行**幂等性**检查（基于 `message_uid`），然后写入 **MySQL**。
   - 成功写入 MySQL 后，**同时写入 Redis 缓存**（例如 `ZSET`）。
3. **重构历史记录 API：**
   - 重构“获取聊天记录”的 HTTP API，使其优先从 Redis 读取，Redis 未命中再查询 MySQL。



#### 阶段六：SQL 调优与理论总结 (约 5%)



**目标：** 优化 SQL 并完成理论文档。

1. **MyBatis XML：** 按要求，将所有 IM 历史查询、会话列表查询等复杂 SQL，全部用原生 SQL 放入 XML 中。使用 `EXPLAIN` 进行调优。
2. **撰写文档：**
   - 回答所有理论问题 (Q1-Q8)。
   - **重点：** 你的文档现在必须重点论述**为什么选择 Netty**（高性能、高并发、掌控底层 I/O），以及你是如何解决 Netty 带来的**安全集成难题**的。



#### 阶段七：Bonus 冲刺 (可选)



- **敏感词：** 在 `MessageService` 中（发送到 MQ 之前）加入敏感词过滤逻辑。
- **IP 归属地：** 在 Netty 握手鉴权时，从 `Channel` 中获取 IP 地址并查询归属地。
- **未读数：** 在 `MessageService` 中，当转发消息发现用户 Channel 不存在（离线）时，使用 Redis 的 `INCR` 增加其未读计数。

这个规划的挑战性极高，特别是阶段二。祝你好运！

你希望我先详细讲解**阶段二（Netty WebSocket 服务器与安全集成）**的关键代码和配置吗？