package org.oplearn.project.facade;

import org.oplearn.project.dto.request.StoreTableRequest;
import org.oplearn.project.dto.response.StoreTableResponse;
import org.oplearn.project.dto.response.TableLandingResponse;

import java.util.List;

public interface StoreTableFacadeService {

  StoreTableResponse create(StoreTableRequest request);

  StoreTableResponse update(Long id, StoreTableRequest request);

  void delete(Long id);

  StoreTableResponse detail(Long id);

  StoreTableResponse getByQrToken(String qrToken);

  List<StoreTableResponse> getAll();

  TableLandingResponse getTableLanding(String qrToken);

  byte[] generateTableQrCard(Long tableId, int qrSize);

  byte[] generateTableQrPure(Long tableId, int qrSize);
}
