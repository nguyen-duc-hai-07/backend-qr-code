package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.StoreTableRequest;
import org.oplearn.project.dto.response.StoreTableResponse;
import org.oplearn.project.entity.StoreTable;
import org.oplearn.project.exception.StoreTableAlreadyExistedException;
import org.oplearn.project.exception.StoreTableNotFoundException;
import org.oplearn.project.repository.StoreTableRepository;
import org.oplearn.project.service.QrCodeService;
import org.oplearn.project.service.StoreTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreTableServiceImpl implements StoreTableService {

  private final StoreTableRepository tableRepository;
  private final QrCodeService qrCodeService;

  @Override
  @Transactional
  public StoreTableResponse create(StoreTableRequest request) {
    log.info("(create) table: {}, zone: {}", request.getLocationName(), request.getZone());

    String locationName = request.getLocationName().trim();
    String zone = request.getZone() != null ? request.getZone().trim() : "";
    if (tableRepository.existsByLocationNameAndZoneAndIsDeletedFalse(locationName, zone)) {
      throw new StoreTableAlreadyExistedException();
    }

    // Sinh token ngẫu nhiên không trùng lặp cho mã QR
    String qrToken = UUID.randomUUID().toString();
    while (tableRepository.existsByQrTokenAndIsDeletedFalse(qrToken)) {
      qrToken = UUID.randomUUID().toString();
    }

    StoreTable table = StoreTable.builder()
        .zone(request.getZone())
        .locationName(locationName)
        .qrToken(qrToken)
        .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
        .build();

    return StoreTableResponse.from(tableRepository.save(table));
  }

  @Override
  @Transactional
  public StoreTableResponse update(Long id, StoreTableRequest request) {
    log.info("(update) table id: {}", id);
    StoreTable table = tableRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(StoreTableNotFoundException::new);

    String locationName = request.getLocationName().trim();
    String zone = request.getZone() != null ? request.getZone().trim() : "";
    if ((!table.getLocationName().equalsIgnoreCase(locationName) || !table.getZone().equalsIgnoreCase(zone))
        && tableRepository.existsByLocationNameAndZoneAndIdNotAndIsDeletedFalse(locationName, zone, id)) {
      throw new StoreTableAlreadyExistedException();
    }

    table.setZone(request.getZone());
    table.setLocationName(locationName);
    if (request.getIsActive() != null) {
      table.setIsActive(request.getIsActive());
    }

    return StoreTableResponse.from(tableRepository.save(table));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(delete) table id: {}", id);
    StoreTable table = tableRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(StoreTableNotFoundException::new);
    table.setIsDeleted(Boolean.TRUE);
    tableRepository.save(table);
  }

  @Override
  public StoreTableResponse detail(Long id) {
    StoreTable table = tableRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(StoreTableNotFoundException::new);
    return StoreTableResponse.from(table);
  }

  @Override
  public List<StoreTableResponse> getAll() {
    return tableRepository.findAllByIsDeletedFalse().stream()
        .map(table -> StoreTableResponse.from(table))
        .toList();
  }

  @Override
  public StoreTableResponse getByQrToken(String qrToken) {
    log.info("(getByQrToken) token: {}", qrToken);
    StoreTable table = tableRepository.findByQrTokenAndIsDeletedFalse(qrToken)
        .orElseThrow(StoreTableNotFoundException::new);
    return StoreTableResponse.from(table);
  }

  @Override
  public byte[] getQrCardImage(Long tableId) {
    StoreTable table = tableRepository.findByIdAndIsDeletedFalse(tableId)
        .orElseThrow(StoreTableNotFoundException::new);

    return qrCodeService.generateTableQrCard(
        table.getQrToken(),
        table.getLocationName(),
        table.getZone()
    );
  }

  @Override
  public byte[] getPureQrCodeImage(Long tableId, int size) {
    StoreTable table = tableRepository.findByIdAndIsDeletedFalse(tableId)
        .orElseThrow(StoreTableNotFoundException::new);

    return qrCodeService.generatePureQrCode(table.getQrToken(), size);
  }
}
