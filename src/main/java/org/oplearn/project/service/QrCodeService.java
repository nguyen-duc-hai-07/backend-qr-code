package org.oplearn.project.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.exception.QrCodeGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QrCodeService {

  @Value("${app.menu-base-url:http://localhost:3001/menu}")
  private String menuBaseUrl;

  /**
   * Tạo ảnh thẻ QR hoàn chỉnh có mã QR + Tên bàn ở dưới để in dán lên bàn
   *
   * @param qrToken      Token bảo mật của bàn
   * @param locationName Tên bàn (VD: "Bàn 01 - Tầng 1")
   * @param zone         Khu vực (VD: "CAFE")
   * @return Mảng byte của ảnh PNG
   */
  public byte[] generateTableQrCard(String qrToken, String locationName, String zone) {
    String url = menuBaseUrl.endsWith("/") ? (menuBaseUrl + qrToken) : (menuBaseUrl + "/" + qrToken);
    String label = locationName + (zone != null && !zone.isBlank() ? " (" + zone + ")" : "");
    String subLabel = "Quét mã để gọi món";

    return createQrCardImage(url, label, subLabel);
  }

  public byte[] generatePureQrCode(String qrToken, int size) {
    String url = menuBaseUrl.endsWith("/") ? (menuBaseUrl + qrToken) : (menuBaseUrl + "/" + qrToken);
    try {
      int s = size > 0 ? size : 300;
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      Map<EncodeHintType, Object> hints = new HashMap<>();
      hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
      hints.put(EncodeHintType.MARGIN, 1);

      BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, s, s, hints);
      BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(qrImage, "PNG", baos);
      return baos.toByteArray();
    } catch (Exception e) {
      log.error("(generatePureQrCode) error: {}", e.getMessage(), e);
      throw new QrCodeGenerationException();
    }
  }

  public byte[] createQrCardImage(String content, String title, String subTitle) {
    try {
      int qrSize = 380;
      int cardWidth = 460;
      int cardHeight = 560;

      // 1. Tạo ma trận QR Code với ZXing
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      Map<EncodeHintType, Object> hints = new HashMap<>();
      hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
      hints.put(EncodeHintType.MARGIN, 1);

      BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
      BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

      // 2. Tạo Canvas ảnh thẻ (Thẻ in)
      BufferedImage cardImage = new BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = cardImage.createGraphics();

      // Bật khử răng cưa nét chữ
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      // Đổ nền trắng toàn bộ thẻ
      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, cardWidth, cardHeight);

      // Viền bo góc nhẹ quanh thẻ
      g2d.setColor(new Color(226, 232, 240)); // Viền xám nhẹ sang trọng
      g2d.setStroke(new BasicStroke(3));
      g2d.drawRoundRect(10, 10, cardWidth - 20, cardHeight - 20, 20, 20);

      // Vẽ ảnh QR Code vào giữa thẻ
      int qrX = (cardWidth - qrSize) / 2;
      int qrY = 30;
      g2d.drawImage(qrImage, qrX, qrY, null);

      // 3. Vẽ Tên bàn in đậm rõ nét ở dưới: VD "Bàn 01 - Tầng 1 (CAFE)"
      g2d.setColor(new Color(15, 23, 42)); // Màu đen than đậm
      Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 22);
      g2d.setFont(titleFont);
      FontMetrics titleMetrics = g2d.getFontMetrics();
      int titleX = (cardWidth - titleMetrics.stringWidth(title)) / 2;
      int titleY = qrY + qrSize + 45;
      g2d.drawString(title, titleX, titleY);

      // 4. Vẽ dòng phụ đề hướng dẫn khách: "Quét mã để gọi món"
      g2d.setColor(new Color(100, 116, 139)); // Màu xám phụ đề
      Font subFont = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
      g2d.setFont(subFont);
      FontMetrics subMetrics = g2d.getFontMetrics();
      int subX = (cardWidth - subMetrics.stringWidth(subTitle)) / 2;
      int subY = titleY + 30;
      g2d.drawString(subTitle, subX, subY);

      g2d.dispose();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(cardImage, "PNG", baos);
      return baos.toByteArray();
    } catch (Exception e) {
      log.error("(createQrCardImage) error: {}", e.getMessage(), e);
      throw new QrCodeGenerationException();
    }
  }
}
