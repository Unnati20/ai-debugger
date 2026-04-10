package com.example.aidebugger.repository;

import com.example.aidebugger.entity.DocumentEntity;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

//     @Query("""
//         SELECT d
//         FROM DocumentEntity d
//         ORDER BY d.embedding <-> :embedding
//         LIMIT 5
//     """)
//     List<DocumentEntity> findTop5BySimilarity(@Param("embedding") List<Double> embedding);

    @Query(value = """
        SELECT *
         FROM documents
        ORDER BY embedding <-> CAST(:embedding AS vector)
         LIMIT 5
        """, nativeQuery = true)
     List<DocumentEntity> findTop5BySimilarity(@Param("embedding") String embeddingLiteral);


//     @Query("""
//         SELECT d.content
//         FROM DocumentEntity d
//         ORDER BY d.embedding <-> :embedding
//         LIMIT 5
//     """)
//     List<String> findTop5ContentsBySimilarity(@Param("embedding") List<Double> embedding);

    @Query(value = """
         SELECT content
         FROM documents
         ORDER BY embedding <-> CAST(:embedding AS vector)
        LIMIT 5
        """, nativeQuery = true)
        List<String> findTop5ContentsBySimilarity(@Param("embedding") String embeddingLiteral);

    List<DocumentEntity> findTop20ByContentContainingIgnoreCaseOrderByIdDesc(String content);

}
