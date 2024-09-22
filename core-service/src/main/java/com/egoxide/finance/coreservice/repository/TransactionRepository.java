package com.egoxide.finance.coreservice.repository;

import com.egoxide.finance.coreservice.domain.Action;
import com.egoxide.finance.coreservice.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query("SELECT DISTINCT t.symbol FROM Transaction t WHERE FUNCTION('YEAR', t.dateTime) = :year AND t.action = :action")
    List<String> findDistinctSymbolsByYearAndAction(@Param("year") int year, @Param("action") Action action);


    @Query("SELECT t FROM Transaction t WHERE t.symbol = :symbol AND t.dateTime <= :endOfYear ORDER BY t.dateTime ASC")
    List<Transaction> findAllChronologicallyBySymbolBeforeYearInclusive(
            @Param("symbol") String symbol,
            @Param("endOfYear") LocalDateTime endOfYear
    );
}
