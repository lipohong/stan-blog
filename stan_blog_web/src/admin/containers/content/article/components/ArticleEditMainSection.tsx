import { FormControlLabel, Switch, TextField, Button, Tooltip, Grid, CircularProgress } from '@mui/material';
import { useEffect, useState } from 'react';
import { useCommonUtils, authed } from '../../../../../commons';
import { FormPanel, HalfRow, OneRow } from '../../../../../components';
import { MultipleTagSelecter } from '../../../../components';
import { IContentMainSectionProps } from '../../commons';
import TopicSelector from '../../components/TopicSelector';
import RichContentEditor from './RichContentEditor';
import * as AiService from '../../../../../services/AiService';

export interface IArticleEditMainSectionProps extends IContentMainSectionProps {
  subTitle: string;
  setSubTitle: (value: string) => void;
  contentProtected: boolean;
  setContentProtected: (value: boolean) => void;
  htmlValue: string;
  setHtmlValue: (value: string) => void;
  setTextValue: (value: string) => void;
  textValue: string;
}

export default function ArticleEditMainSection(props: Readonly<IArticleEditMainSectionProps>) {
  const { t, enqueueSnackbar } = useCommonUtils();
  const [aiLoading, setAiLoading] = useState<boolean>(false);
  const [quotaExceeded, setQuotaExceeded] = useState<boolean>(false);
  const isAdmin = authed(['ROLE_ADMIN']);
  const intervalMs = Number(import.meta.env.GEN_TITLE_QUOTA_CHECK_INTERVAL ?? 5000);

  useEffect(() => {
    if (isAdmin) return;
    const check = () => {
      AiService.checkQuota()
        .then(res => {
          const data = res.data;
          if (data?.success === true) {
            const remaining = typeof data?.data?.remaining === 'number' ? data.data.remaining : (data?.data?.remainingCount ?? 0);
            setQuotaExceeded(remaining <= 0);
          } else {
            setQuotaExceeded(true);
          }
        })
        .catch(() => setQuotaExceeded(true));
    };
    check();
    const timer = setInterval(check, intervalMs);
    return () => clearInterval(timer);
  }, [isAdmin, intervalMs]);

  const contentLen = (props.textValue ?? '').trim().length;
  const tooShort = contentLen <= 100;
  const tooLong = contentLen > 5000;
  const disabled = !props.editable || aiLoading || tooShort || tooLong || (!isAdmin && quotaExceeded);
  const tooltipTitle = tooShort
    ? t('article-creation-page.title-generation.tooltip-title.too-short')
    : tooLong
      ? t('article-creation-page.title-generation.tooltip-title.too-long')
      : !isAdmin && quotaExceeded
        ? t('article-creation-page.title-generation.tooltip-title.no-quota')
        : t('article-creation-page.title-generation.btn');

  return (
    <FormPanel sx={{ mt: 1, ...props.sx }}>
      <OneRow>
        <Grid
          container
          spacing={2}
          alignItems="center"
        >
          <Grid
            item
            xs={12}
            sm={9}
          >
            <TextField
              fullWidth
              label={t('article-creation-page.fields.title')}
              disabled={!props.editable}
              value={props.title}
              onChange={v => props.setTitle(v.target.value)}
              error={props.title.length >= 128}
            />
          </Grid>
          <Grid
            item
            xs={12}
            sm={3}
          >
            <Tooltip title={tooltipTitle}>
              <span>
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  disabled={disabled}
                  onClick={() => {
                    setAiLoading(true);
                    AiService.generateTitle(props.textValue)
                      .then(res => {
                        const data = res.data;
                        if (data?.success === true && typeof data?.data === 'string') {
                          props.setTitle(data.data);
                        } else {
                          enqueueSnackbar(data?.message || t('article-creation-page.title-generation.tooltip-title.generate-title-fail'), { variant: 'error' });
                        }
                      })
                      .catch(() => enqueueSnackbar(t('article-creation-page.title-generation.tooltip-title.server-not-available'), { variant: 'error' }))
                      .finally(() => setAiLoading(false));
                  }}
                  sx={{ height: { xs: 48, sm: 56 } }}
                >
                  {aiLoading ? (
                    <CircularProgress
                      size={20}
                      sx={{ color: 'white' }}
                    />
                  ) : (
                    t('article-creation-page.title-generation.btn')
                  )}
                </Button>
              </span>
            </Tooltip>
          </Grid>
        </Grid>
      </OneRow>
      <OneRow>
        <TextField
          fullWidth
          label={t('article-creation-page.fields.sub-title')}
          disabled={!props.editable}
          value={props.subTitle}
          onChange={v => props.setSubTitle(v.target.value)}
          error={props.subTitle.length >= 1000}
        />
      </OneRow>
      <HalfRow>
        <TextField
          fullWidth
          label={t('article-creation-page.fields.cover-img-url')}
          disabled={!props.editable}
          value={props.coverImgUrl}
          onChange={v => props.setCoverImgUrl(v.target.value)}
          error={props.coverImgUrl?.length >= 2000}
        />
      </HalfRow>
      <HalfRow>
        <TopicSelector
          editable={props.editable}
          topic={props.topic}
          setTopic={props.setTopic}
        />
      </HalfRow>
      <OneRow>
        <MultipleTagSelecter
          disabled={!props.editable}
          value={props.tagValues}
          setValue={props.setTagValues}
        />
      </OneRow>
      <OneRow>
        <RichContentEditor
          htmlValue={props.htmlValue}
          setHtmlValue={props.setHtmlValue}
          setTextValue={props.setTextValue}
          editable={props.editable}
        />
      </OneRow>
      <OneRow>
        <FormControlLabel
          control={
            <Switch
              disabled={!props.editable}
              checked={props.contentProtected}
              onChange={e => props.setContentProtected(e.target.checked)}
            />
          }
          label={t('article-creation-page.fields.content-protected')}
        />
      </OneRow>
    </FormPanel>
  );
}
