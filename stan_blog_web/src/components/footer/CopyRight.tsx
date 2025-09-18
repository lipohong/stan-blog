import { Grid } from '@mui/material';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import { Link as RouterLink } from 'react-router-dom';
import config from '../../../customization.json';
import { URL } from '../../routes';
import packageJson from '../../../package.json';
const packageVersion = packageJson.version;

function Copyright() {
  const { title } = config;
  return (
    <Grid>
      <Typography
        variant="body2"
        color="text.secondary"
        align="center"
      >
        {'Copyright © '}
        {new Date().getFullYear()}{' '}
        <Link
          component={RouterLink}
          color="inherit"
          to={URL.HOME}
        >
          <strong>{title.toUpperCase()}</strong>
        </Link>
        {` Version:`}
        <strong>{` v${packageVersion}`}</strong>
      </Typography>
    </Grid>
  );
}

export default Copyright;
