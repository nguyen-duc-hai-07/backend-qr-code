package org.oplearn.project.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.StoreTableRequest;
import org.oplearn.project.dto.response.CategoryResponse;
import org.oplearn.project.dto.response.ProductResponse;
import org.oplearn.project.dto.response.StoreTableResponse;
import org.oplearn.project.dto.response.TableLandingResponse;
import org.oplearn.project.facade.StoreTableFacadeService;
import org.oplearn.project.service.CategoryService;
import org.oplearn.project.service.ProductService;
import org.oplearn.project.service.StoreTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreTableFacadeServiceImpl implements StoreTableFacadeService {

  private final StoreTableService storeTableService;
  private final CategoryService categoryService;
  private final ProductService productService;

  @Override
  @Transactional
  public StoreTableResponse create(StoreTableRequest request) {
    log.info("(StoreTableFacade) create table: {}", request.getLocationName());
    return storeTableService.create(request);
  }

  @Override
  @Transactional
  public StoreTableResponse update(Long id, StoreTableRequest request) {
    log.info("(StoreTableFacade) update table id: {}", id);
    return storeTableService.update(id, request);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(StoreTableFacade) delete table id: {}", id);
    storeTableService.delete(id);
  }

  @Override
  public StoreTableResponse detail(Long id) {
    return storeTableService.detail(id);
  }

  @Override
  public StoreTableResponse getByQrToken(String qrToken) {
    return storeTableService.getByQrToken(qrToken);
  }

  @Override
  public List<StoreTableResponse> getAll() {
    return storeTableService.getAll();
  }

  @Override
  public TableLandingResponse getTableLanding(String qrToken) {
    log.info("(StoreTableFacade) getTableLanding qrToken: {}", qrToken);

    // 1. Lấy thông tin bàn
    StoreTableResponse table = storeTableService.getByQrToken(qrToken);

    // 2. Lấy danh mục đang hoạt động
    List<CategoryResponse> categories = categoryService.getAll(true);

    // 3. Lấy tất cả món ăn có sẵn
    List<ProductResponse> products = productService.getAll(null, null, true);

    return TableLandingResponse.builder()
        .table(table)
        .categories(categories)
        .products(products)
        .build();
  }

  @Override
  public byte[] generateTableQrCard(Long tableId, int qrSize) {
    return storeTableService.getQrCardImage(tableId);
  }

  @Override
  public byte[] generateTableQrPure(Long tableId, int qrSize) {
    return storeTableService.getPureQrCodeImage(tableId, qrSize);
  }
}
