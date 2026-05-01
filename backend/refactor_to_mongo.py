import os
import re

src_dir = r"c:\GIT HUB\FlowCharge\backend\src\main\java\com\meterflow"

for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()

            new_content = content
            
            # JPA to Mongo annotations
            new_content = re.sub(r'import jakarta\.persistence\.\*;', 'import org.springframework.data.annotation.*;\nimport org.springframework.data.mongodb.core.mapping.*;\nimport org.springframework.data.mongodb.core.index.Indexed;', new_content)
            new_content = re.sub(r'@Entity', '@Document', new_content)
            new_content = re.sub(r'@Table\(name\s*=\s*"([^"]+)"[^\)]*\)', r'@Document(collection = "\1")', new_content)
            new_content = re.sub(r'@Table\(name\s*=\s*"([^"]+)"\)', r'@Document(collection = "\1")', new_content)
            new_content = re.sub(r'@GeneratedValue[^)]*\)', '', new_content)
            new_content = re.sub(r'@Column\(.*unique\s*=\s*true.*\)', '@Indexed(unique = true)', new_content)
            new_content = re.sub(r'@Column\([^)]*\)', '', new_content)
            new_content = re.sub(r'@Enumerated\([^)]*\)', '', new_content)
            new_content = re.sub(r'@ManyToOne\([^)]*\)', '@DBRef', new_content)
            new_content = re.sub(r'@JoinColumn\([^)]*\)', '', new_content)
            new_content = re.sub(r'@PrePersist', '', new_content)
            
            # Repositories
            new_content = re.sub(r'JpaRepository<([^,]+),\s*Long>', r'MongoRepository<\1, String>', new_content)
            new_content = re.sub(r'import org\.springframework\.data\.jpa\.repository\.JpaRepository;', 'import org.springframework.data.mongodb.repository.MongoRepository;', new_content)
            
            # Long ID to String ID
            new_content = re.sub(r'Long\s+id', 'String id', new_content)
            new_content = re.sub(r'Long\s+apiId', 'String apiId', new_content)
            # Find methods taking Long id
            new_content = re.sub(r'\(Long\s+id', '(String id', new_content)
            new_content = re.sub(r'\(Long\s+apiId', '(String apiId', new_content)
            
            # DTOs
            new_content = re.sub(r'Long\s+id\s*,', 'String id,', new_content)
            new_content = re.sub(r'Long\s+apiId\s*,', 'String apiId,', new_content)
            new_content = re.sub(r'Long\s+apiId\s*\)', 'String apiId)', new_content)
            new_content = re.sub(r'Long\s+id\s*\)', 'String id)', new_content)

            if new_content != content:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
