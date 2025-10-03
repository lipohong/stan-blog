import { useCallback, useMemo, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Grid } from '@mui/material';
import { enqueueSnackbar } from 'notistack';
import { useCommonUtils } from '../../../commons';
import AuthTextField from '../../../portal/components/auth/AuthTextField';
import * as UserService from '../../../services/UserService';

interface UserCreateDialogProps {
  readonly open: boolean;
  readonly onClose: () => void;
  readonly onCreated?: () => void; // parent can reload list
}

export default function UserCreateDialog({ open, onClose, onCreated }: UserCreateDialogProps) {
  const { t } = useCommonUtils();
  const [firstName, setFirstName] = useState<string>('');
  const [lastName, setLastName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);

  const resetForm = useCallback(() => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
  }, []);

  const emailValid = useMemo(() => {
    const emailReg = /^(\w-*\.*)+@(\w-?)+(\.\w{2,})+$/;
    return emailReg.test(email.trim());
  }, [email]);

  const passwordValid = useMemo(() => password.length >= 6 && password.length <= 64, [password]);
  const passwordMatch = useMemo(() => password === confirmPassword, [password, confirmPassword]);

  const handleClose = useCallback(() => {
    if (submitting) return;
    resetForm();
    onClose();
  }, [onClose, resetForm, submitting]);

  const handleSubmit = useCallback(() => {
    if (!emailValid) {
      enqueueSnackbar(t('sign-up.msg.warning.invalid-email-format'), { variant: 'warning' });
      return;
    }
    if (!passwordMatch) {
      enqueueSnackbar(t('sign-up.msg.warning.two-password-dismatch'), { variant: 'warning' });
      return;
    }
    if (!passwordValid) {
      enqueueSnackbar(t('sign-up.msg.warning.invalid-password-length'), { variant: 'warning' });
      return;
    }

    setSubmitting(true);
    UserService.createUser({
      firstName: firstName || undefined,
      lastName: lastName || undefined,
      email: email || undefined,
      password: password || undefined,
    })
      .then(() => {
        enqueueSnackbar(t('user-create.msg.success'), { variant: 'success', autoHideDuration: 4000 });
        onCreated && onCreated();
        resetForm();
        onClose();
      })
      .catch((error: any) => {
        if (error?.response?.status === 409) {
          enqueueSnackbar(t('sign-up.msg.email-exists'), { variant: 'warning' });
        } else {
          enqueueSnackbar(t('user-create.msg.error'), { variant: 'error' });
        }
      })
      .finally(() => setSubmitting(false));
  }, [emailValid, passwordMatch, passwordValid, firstName, lastName, email, password, onCreated, onClose, t, resetForm]);

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="sm"
    >
      <DialogTitle>{t('user-create.title')}</DialogTitle>
      <DialogContent>
        <Grid
          container
          spacing={2}
          sx={{ mt: 0.5 }}
        >
          <Grid
            item
            xs={12}
            sm={6}
          >
            <AuthTextField
              autoComplete="given-name"
              name="firstName"
              id="firstName"
              label={t('sign-up.fields.first-name')}
              value={firstName}
              onChange={e => setFirstName(e.target.value)}
            />
          </Grid>
          <Grid
            item
            xs={12}
            sm={6}
          >
            <AuthTextField
              id="lastName"
              label={t('sign-up.fields.last-name')}
              name="lastName"
              autoComplete="family-name"
              value={lastName}
              onChange={e => setLastName(e.target.value)}
            />
          </Grid>
          <Grid
            item
            xs={12}
          >
            <AuthTextField
              required
              id="email"
              label={t('sign-up.fields.email')}
              name="email"
              autoComplete="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              error={!!email && !emailValid}
            />
          </Grid>
          <Grid
            item
            xs={12}
          >
            <AuthTextField
              required
              name="password"
              label={t('sign-up.fields.password')}
              type="password"
              id="password"
              autoComplete="new-password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              error={!!password && !passwordValid}
              helperText={!passwordValid && password ? t('sign-up.msg.warning.invalid-password-length') : ''}
            />
          </Grid>
          <Grid
            item
            xs={12}
          >
            <AuthTextField
              required
              name="password-confirm"
              label={t('sign-up.fields.password-confirm')}
              type="password"
              id="password-confirm"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              error={!!confirmPassword && !passwordMatch}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={handleClose}
          disabled={submitting}
        >
          {t('components.standard-button.cancel')}
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={submitting || !email || !password || !confirmPassword}
        >
          {t('components.standard-button.create')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
