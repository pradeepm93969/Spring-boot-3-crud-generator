package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RepositoryGeneratorService {

    public void generateRepository(CRUDGenerationRequest request) {
        StringBuilder repositoryClass = new StringBuilder();

        repositoryClass.append("package ").append(request.getPackageName()).append(".jpa.repository")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(";\n\n");
        repositoryClass.append("import ").append(request.getPackageName()).append(".jpa.domain")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(".Jpa")
                .append(request.getEntityName()).append(";\n");
        repositoryClass.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        repositoryClass.append("import org.springframework.data.jpa.repository.JpaSpecificationExecutor;\n\n");

        repositoryClass.append("public interface ").append(request.getEntityName()).append("Repository extends JpaRepository<Jpa")
                .append(request.getEntityName()).append(", String>, JpaSpecificationExecutor<Jpa").append(request.getEntityName()).append("> {\n");
        repositoryClass.append("}\n");

        // Write request class to file
        String filePath = request.getDirectory() + "\\jpa\\repository"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? "\\"
                + request.getSubPackageName() : "")
                + "\\" + request.getEntityName() + "Repository.java";
        FileUtils.writeFile(repositoryClass.toString(), filePath);

    }

}
