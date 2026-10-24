package campus.content;

/**
 * 證物卡。
 *
 * @param kind         {@code note}、{@code document} 或 {@code newspaper}，決定卡片外觀
 * @param unlockOnStep 解開這個步驟後才出現；為 null 時一開始就在桌上
 */
public record Evidence(String id, String kind, String title, String content, String unlockOnStep) {}
