import { Dialog, DialogContent, DialogTitle, Grid, TextField } from '@mui/material';
import { useState } from 'react';
import { SaveButtonPanel } from '../../../admin/components';
import { useCommonUtils } from '../../../commons';
import * as WordService from './WordService';

interface WordCreateDialogProps {
  readonly vocabularyId: string;
  readonly loadData: (vocabularyId: string) => void;
  readonly open: boolean;
  readonly setOpen: React.Dispatch<React.SetStateAction<boolean>>;
}

export function WordCreateDialog({ vocabularyId, loadData, open, setOpen }: WordCreateDialogProps) {
  const { t, enqueueSnackbar } = useCommonUtils();
  const [text, setText] = useState<string>('');
  const [partOfSpeech, setPartOfSpeech] = useState<string>('');
  const [meaningInChinese, setMeaningInChinese] = useState<string>('');
  const [meaningInEnglish, setMeaningInEnglish] = useState<string>('');

  function handleClose() {
    setText('');
    setPartOfSpeech('');
    setMeaningInChinese('');
    setMeaningInEnglish('');
    setOpen(false);
  }

  function handleSave() {
    WordService.createWord({
      vocabularyId,
      text,
      partOfSpeech,
      meaningInChinese,
      meaningInEnglish,
    })
      .then(() => {
        enqueueSnackbar(t('msg.success'), { variant: 'success' });
        handleClose();
        loadData(vocabularyId);
      })
      .catch(() => {
        enqueueSnackbar(t('msg.error'), {
          variant: 'error',
        });
      });
  }

  return (
    <Grid
      item
      sx={{ mb: 1 }}
    >
      <Dialog
        fullWidth={true}
        maxWidth={'sm'}
        open={open}
        onClose={handleClose}
      >
        <DialogTitle>{t('word-dialog.title')}</DialogTitle>
        <DialogContent>
          <Grid
            container
            spacing={1}
          >
            <Grid
              item
              xs={6}
            >
              <TextField
                margin="dense"
                id="word"
                label={t('word-dialog.fields.word')}
                fullWidth
                value={text}
                onChange={e => setText(e.target.value)}
              />
            </Grid>
            <Grid
              item
              xs={6}
            >
              <TextField
                margin="dense"
                id="partOfSpeech"
                label={t('word-dialog.fields.partOfSpeech')}
                fullWidth
                value={partOfSpeech}
                onChange={e => setPartOfSpeech(e.target.value)}
              />
            </Grid>
            <Grid
              item
              xs={12}
            >
              <TextField
                margin="dense"
                id="meaningInChinese"
                label={t('word-dialog.fields.meaningInChinese')}
                fullWidth
                value={meaningInChinese}
                onChange={e => setMeaningInChinese(e.target.value)}
              />
            </Grid>
            <Grid
              item
              xs={12}
            >
              <TextField
                margin="dense"
                id="meaningInEnglish"
                label={t('word-dialog.fields.meaningInEnglish')}
                fullWidth
                value={meaningInEnglish}
                onChange={e => setMeaningInEnglish(e.target.value)}
              />
            </Grid>
          </Grid>
          <SaveButtonPanel
            primaryEvent={handleSave}
            secondaryEvent={handleClose}
          />
        </DialogContent>
      </Dialog>
    </Grid>
  );
}
