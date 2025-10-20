import { Grid, Paper, TextField, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { TimeFormat, useCommonUtils } from '../../../../../commons';
import { IPlanProgress } from '../../../../../global/types';
import { EditIconButton, InputFileUploadButton } from '../../../../components';
import * as PlanProgressService from './PlanProgressService.ts';
import * as FileService from './FileService.ts';
import { ImagesPanel } from './ImagesPanel';

interface IPlanProgressItemProps {
  progress: IPlanProgress;
  viewOnly: boolean;
}

export default function PlanProgressItem(props: Readonly<IPlanProgressItemProps>) {
  const { t, enqueueSnackbar } = useCommonUtils();
  const [progress, setProgress] = useState<IPlanProgress>(props.progress);
  const [editable, setEditable] = useState<boolean>(false);
  const [progressDesc, setProgressDesc] = useState<string>(props.progress.description);
  const [images, setImages] = useState<{ id: number | string; url: string }[]>([]);

  useEffect(() => {
    if (!props.progress?.id) return;
    FileService.listBySource(props.progress.id, 'PLAN_PIC', 1, 50)
      .then(response => {
        const imgs = (response?.data?.records ?? []).filter((r: any) => !!r.viewUrl).map((r: any) => ({ id: r.id, url: formatUrl(r.viewUrl) }));
        setImages(imgs);
      })
      .catch(() => {
        // ignore errors for image loading
      });
  }, [props.progress?.id]);

  function onImageChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    if (!progress?.id || files.length < 1) return;
    FileService.batchUpload(progress.id, 'PLAN_PIC', files, true)
      .then(() => {
        return FileService.listBySource(progress.id, 'PLAN_PIC', 1, 50);
      })
      .then(response => {
        const imgs = (response?.data?.records ?? []).filter((r: any) => !!r.viewUrl).map((r: any) => ({ id: r.id, url: formatUrl(r.viewUrl) }));
        setImages(imgs);
        enqueueSnackbar(t('msg.success'), { variant: 'success' });
      })
      .catch(() => {
        enqueueSnackbar(t('msg.error'), { variant: 'error' });
      });
  }

  const formatUrl = (u: string) => `${import.meta.env.VITE_SERVER_ROOT_URL.replace(/\/$/, '')}/${u.replace(/^\//, '')}`;

  function handleDeleteImage(fileId: number | string) {
    if (!progress?.id) return;
    FileService.deleteById(fileId)
      .then(() => FileService.listBySource(progress.id, 'PLAN_PIC', 1, 50))
      .then(response => {
        const imgs = (response?.data?.records ?? []).filter((r: any) => !!r.viewUrl).map((r: any) => ({ id: r.id, url: formatUrl(r.viewUrl) }));
        setImages(imgs);
        enqueueSnackbar(t('msg.success'), { variant: 'success' });
      })
      .catch(() => {
        enqueueSnackbar(t('msg.error'), { variant: 'error' });
      });
  }

  const handleEditableChange = () => {
    if (editable) {
      handleProgressUpdate();
    }
    setEditable(!editable);
  };

  function handleProgressUpdate(): void {
    PlanProgressService.updateProgress({
      id: progress.id,
      planId: progress.planId,
      description: progressDesc,
    })
      .then(response => {
        enqueueSnackbar(t('msg.success'), {
          variant: 'success',
        });
        setProgress(response.data);
      })
      .catch(() => {
        enqueueSnackbar(t('msg.error'), {
          variant: 'error',
        });
      });
  }

  return (
    <Paper
      sx={{
        py: 3,
        px: { sm: 3, xs: 2 },
        borderWidth: { xs: '0px', sm: '1px' },
        borderBottomWidth: { xs: '1px' },
        borderRadius: 4,
      }}
    >
      <Grid
        container
        item
        xs={12}
      >
        <Grid
          container
          item
          xs={12}
          justifyContent="space-between"
          alignItems="center"
        >
          <Typography
            color="text.secondary"
            variant="body2"
          >
            {TimeFormat.dateFormat(progress.createTime)} {TimeFormat.timeFormat(progress.createTime)}
          </Typography>
          {!props.viewOnly && (
            <EditIconButton
              editable={editable}
              handleEditableChange={handleEditableChange}
            />
          )}
        </Grid>
        <Grid
          item
          xs={12}
        >
          {!props.viewOnly ? (
            <TextField
              fullWidth
              variant="standard"
              multiline
              minRows={2}
              maxRows={20}
              disabled={!editable}
              value={progressDesc}
              onChange={event => setProgressDesc(event.target.value)}
            />
          ) : (
            <Typography
              color="text.secondary"
              variant="body2"
              sx={{ whiteSpace: 'pre-wrap' }}
            >
              {progressDesc}
            </Typography>
          )}
        </Grid>
        {!props.viewOnly && editable && (
          <Grid
            item
            container
            xs={12}
            justifyContent="flex-end"
            sx={{ mt: 1 }}
          >
            <InputFileUploadButton onImageChange={onImageChange} />
          </Grid>
        )}
        <ImagesPanel
          keyPrefix={props.progress.id}
          images={images}
          editable={!props.viewOnly && editable}
          onDelete={handleDeleteImage}
        />
      </Grid>
    </Paper>
  );
}
