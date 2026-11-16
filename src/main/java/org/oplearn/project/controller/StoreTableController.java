package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.StoreTableRequest;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.dto.response.StoreTableResponse;
import org.oplearn.project.dto.response.TableLandingResponse;
import org.oplearn.project.facade.StoreTableFacadeService;
import org.oplearn.project.service.StoreTableService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/tables")
public class StoreTableController {

  private final StoreTableFacadeService facade;
  private final StoreTableService service;

  // ====================================================================
  // PUBLIC APIs: Dành cho Khách hàng khi quét mã QR trên bàn
  // ====================================================================

  /**
   * Lấy thông tin cơ bản của bàn khi quét mã QR.
   */
  @GetMapping("/qr/{qrToken}")
  public ResponseGeneral<StoreTableResponse> getTableByQrToken(
      @PathVariable String qrToken
  ) {
    log.info("(getTableByQrToken) customer scanned qr token: {}", qrToken);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getByQrToken(qrToken));
  }

  /**
   * Landing Page API: Sau khi khách quét mã QR, trả về TRỌN GÓI trong 1 request:
   * 1. Thông tin bàn (Tên bàn, khu vực)
   * 2. Danh mục món ăn đang hoạt động (để render các tab filter)
   * 3. Toàn bộ danh sách sản phẩm/món ăn có sẵn trong quán
   */
  @GetMapping("/qr/{qrToken}/landing")
  public ResponseGeneral<TableLandingResponse> getTableLanding(
      @PathVariable String qrToken
  ) {
    log.info("(getTableLanding) customer scanned qr token: {}", qrToken);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getTableLanding(qrToken));
  }

  // ====================================================================
  // ADMIN APIs: Quản lý bàn và Xuất ảnh thẻ QR để in ấn
  // ====================================================================

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<StoreTableResponse> create(
      @Valid @RequestBody StoreTableRequest request
  ) {
    log.info("(create) table: {}", request.getLocationName());
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<StoreTableResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody StoreTableRequest request
  ) {
    log.info("(update) id: {}, table: {}", id, request.getLocationName());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) id: {}", id);
    facade.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping
  public ResponseGeneral<List<StoreTableResponse>> getAll() {
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getAll());
  }

  @GetMapping("/{id}")
  public ResponseGeneral<StoreTableResponse> detail(@PathVariable Long id) {
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id));
  }

  /**
   * API Tải hoặc Xem trực tiếp ảnh QR (Mã QR thuần hoặc Thẻ in đầy đủ).
   * produces = MediaType.IMAGE_PNG_VALUE, permitAll() cho phép browser render trực tiếp vào thẻ <img>
   * @param type "pure" (mã QR thuần nét) hoặc "card" (thẻ in hoàn chỉnh có viền & tên bàn)
   */
  @GetMapping(value = "/{id}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> getTableQrCodeCard(
      @PathVariable Long id,
      @RequestParam(value = "size", defaultValue = "300") int size,
      @RequestParam(value = "type", defaultValue = "pure") String type
  ) {
    log.info("(getTableQrCodeCard) table id: {}, size: {}, type: {}", id, size, type);
    byte[] imageBytes = "card".equalsIgnoreCase(type)
        ? facade.generateTableQrCard(id, size)
        : facade.generateTableQrPure(id, size);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"table-" + id + "-qr.png\"")
        .contentType(MediaType.IMAGE_PNG)
        .body(imageBytes);
  }
}
