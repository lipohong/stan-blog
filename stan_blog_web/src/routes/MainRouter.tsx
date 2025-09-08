import { Suspense } from 'react';
import { Routes } from 'react-router-dom';

import { getAdminRoutes } from '../admin/AdminRoutes';
import { Loading } from '../components';
import getPortalRoutes from '../portal/PortalRoutes';

export function MainRouter() {
  return (
    <Suspense fallback={<Loading open={true} />}>
      <Routes>
        {getPortalRoutes()}
        {getAdminRoutes()}
      </Routes>
    </Suspense>
  );
}
