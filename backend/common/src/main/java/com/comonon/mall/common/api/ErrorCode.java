package com.comonon.mall.common.api;

/**
 * 业务错误码常量，对应 auth-design.md §9。
 */
public final class ErrorCode {
    private ErrorCode() {}

    public static final int OK = 0;
    public static final int BAD_REQUEST = 40000;
    public static final int UNAUTHORIZED = 40100;

    /** 验证码错误 */
    public static final int SMS_CODE_INVALID = 40101;
    /** 验证码已过期 */
    public static final int SMS_CODE_EXPIRED = 40102;
    /** 发送过于频繁 */
    public static final int SMS_SEND_TOO_FREQUENT = 40103;
    /** 短信发送上限 */
    public static final int SMS_SEND_LIMIT_EXCEEDED = 40104;

    /** refreshToken 无效，需重登 */
    public static final int REFRESH_TOKEN_INVALID = 40110;
    /** access 已失效（黑名单或 session 不存在） */
    public static final int ACCESS_TOKEN_INVALID = 40111;

    /** 账号已禁用 */
    public static final int ACCOUNT_DISABLED = 40301;
    /** 账号已注销 */
    public static final int ACCOUNT_DEACTIVATED = 40302;

    public static final int INTERNAL_ERROR = 50000;

    /** 商品不存在或不可售 */
    public static final int SPU_NOT_FOUND = 40401;
    /** SKU 不存在 */
    public static final int SKU_NOT_FOUND = 40402;
    /** 类目不存在 */
    public static final int CATEGORY_NOT_FOUND = 40403;

    /** 不满足上架条件 */
    public static final int SPU_NOT_PUBLISHABLE = 40901;
    /** 规格重复 */
    public static final int SKU_SPEC_DUPLICATE = 40902;
    /** 类目有子节点不可删 */
    public static final int CATEGORY_HAS_CHILDREN = 40903;
    /** 类目下有商品不可删 */
    public static final int CATEGORY_HAS_SPU = 40907;
    /** 禁止修改商品类型 */
    public static final int PRODUCT_TYPE_IMMUTABLE = 40908;
    /** 上架中商品不可删 SKU */
    public static final int SKU_DELETE_FORBIDDEN = 40909;

    /** 地址不存在 */
    public static final int ADDRESS_NOT_FOUND = 40411;
    /** 地址数量超限 */
    public static final int ADDRESS_LIMIT_EXCEEDED = 40910;

    /** 购物车 SKU 行数超限 */
    public static final int CART_ITEM_LIMIT = 40920;
    /** SKU 不可售 */
    public static final int SKU_NOT_SELLABLE = 40921;
    /** 未勾选结算项 */
    public static final int CART_NOTHING_SELECTED = 40922;
    /** 购物车库存不足 */
    public static final int CART_STOCK_INSUFFICIENT = 40923;
    /** 购物车无此行 */
    public static final int CART_ITEM_NOT_FOUND = 40424;
    /** 购物车不可混放不同类型商品 */
    public static final int CART_MIXED_PRODUCT_TYPE = 40924;
    /** 虚拟/服务类商品限购 1 件 */
    public static final int CART_VIRTUAL_QTY_LIMIT = 40925;

    /** 订单不存在 */
    public static final int ORDER_NOT_FOUND = 40431;
    /** 支付单不存在 */
    public static final int PAYMENT_NOT_FOUND = 40432;

    /** 锁库存不足 */
    public static final int STOCK_LOCK_INSUFFICIENT = 40904;
    /** 订单状态不允许该操作 */
    public static final int ORDER_STATUS_ILLEGAL = 40930;
    /** 无可下单商品 */
    public static final int ORDER_EMPTY_ITEMS = 40931;
    /** 支付状态不允许该操作 */
    public static final int PAYMENT_STATE_ILLEGAL = 40932;

    /** 评价不存在 */
    public static final int REVIEW_NOT_FOUND = 40440;
    /** 订单不可评价 */
    public static final int REVIEW_NOT_ALLOWED = 40940;
    /** 已评价过 */
    public static final int REVIEW_ALREADY_EXISTS = 40941;

    /** 虚拟卡密不存在 */
    public static final int VIRTUAL_CARD_NOT_FOUND = 40433;
    /** 虚拟商品卡密库存不足 */
    public static final int VIRTUAL_CARD_NOT_AVAILABLE = 40933;
    /** 卡密必须绑定虚拟商品 */
    public static final int VIRTUAL_CARD_SPU_INVALID = 40934;

    /** 核销码不存在 */
    public static final int SERVICE_VERIFY_NOT_FOUND = 40434;
    /** 服务商品核销码库存不足 */
    public static final int SERVICE_VERIFY_NOT_AVAILABLE = 40935;
    /** 核销码必须绑定服务商品 */
    public static final int SERVICE_VERIFY_SPU_INVALID = 40936;
}
