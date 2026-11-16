package org.oplearn.project.service;

import org.oplearn.project.dto.request.StoreTableRequest;
import org.oplearn.project.dto.response.StoreTableResponse;

import java.util.List;

public interface StoreTableService {

  StoreTableResponse create(StoreTableRequest request);

  StoreTableResponse update(Long id, StoreTableRequest request);

  void delete(Long id);

  StoreTableResponse detail(Long id);

  List<StoreTableResponse> getAll();

  StoreTableResponse getByQrToken(String qrToken);

  byte[] getQrCardImage(Long tableId);

  byte[] getPureQrCodeImage(Long tableId, int size);
}
