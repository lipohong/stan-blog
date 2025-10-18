package com.stan.blog.content.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.WordCreationDTO;
import com.stan.blog.beans.dto.content.WordDTO;
import com.stan.blog.beans.dto.content.WordUpdateDTO;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.content.WordEntity;
import com.stan.blog.beans.repository.content.WordRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.BasicConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final ContentGeneralInfoService contentGeneralInfoService;

    @Transactional
    public WordDTO saveWord(WordCreationDTO dto) {
        validateCreationDTO(dto);

        ContentGeneralInfoEntity contentEntity = contentGeneralInfoService.findById(dto.getVocabularyId());
        if (Objects.isNull(contentEntity) || !ContentType.VOCABULARY.name().equals(contentEntity.getContentType())) {
            throw new StanBlogRuntimeException("The vocabularyId is invalid");
        }
        WordEntity entity = BasicConverter.convert(dto, WordEntity.class);
        WordEntity saved = wordRepository.save(entity);
        return getWordById(saved.getId());
    }

    @Transactional
    public WordDTO updateWord(WordUpdateDTO dto) {
        validateUpdateDTO(dto);

        WordEntity existing = wordRepository.findById(dto.getId())
                .orElseThrow(() -> new StanBlogRuntimeException("Word not found"));
        existing.setText(dto.getText());
        existing.setMeaningInChinese(dto.getMeaningInChinese());
        existing.setMeaningInEnglish(dto.getMeaningInEnglish());
        existing.setPartOfSpeech(dto.getPartOfSpeech());
        WordEntity updated = wordRepository.save(existing);
        return getWordById(updated.getId());
    }

    @Transactional(readOnly = true)
    public List<WordDTO> getWordsByVOCId(String vocabularyId) {
        return wordRepository.findByVocabularyId(vocabularyId).stream()
                .map(word -> BasicConverter.convert(word, WordDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WordDTO getWordById(Long id) {
        return wordRepository.findById(id)
                .map(word -> BasicConverter.convert(word, WordDTO.class))
                .orElse(null);
    }

    @Transactional
    public boolean deleteWordById(Long id) {
        if (!wordRepository.findById(id).isEmpty()) {
            wordRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validateCreationDTO(WordCreationDTO dto) {
        if (StringUtils.isBlank(dto.getVocabularyId()) || StringUtils.isBlank(dto.getText())) {
            throw new StanBlogRuntimeException("VocabularyId and text must not be null");
        }
        validateCommonFields(dto.getText(), dto.getMeaningInChinese(),
                dto.getMeaningInEnglish(), dto.getPartOfSpeech());
    }

    private void validateUpdateDTO(WordUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new StanBlogRuntimeException("Id must not be null for update");
        }
        validateCommonFields(dto.getText(), dto.getMeaningInChinese(),
                dto.getMeaningInEnglish(), dto.getPartOfSpeech());
    }

    private void validateCommonFields(String text, String meaningCn, String meaningEn, String partOfSpeech) {
        if (text != null && text.length() > 200) {
            throw new StanBlogRuntimeException("Text length can not exceed 200");
        }
        if (meaningCn != null && meaningCn.length() > 128) {
            throw new StanBlogRuntimeException("MeaningInChinese length can not exceed 128");
        }
        if (meaningEn != null && meaningEn.length() > 128) {
            throw new StanBlogRuntimeException("MeaningInEnglish length can not exceed 128");
        }
        if (partOfSpeech != null && partOfSpeech.length() > 32) {
            throw new StanBlogRuntimeException("PartOfSpeech length can not exceed 32");
        }
    }
}
