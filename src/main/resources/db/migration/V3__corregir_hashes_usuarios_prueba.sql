update usuarios
set password = '$2a$10$ZYLUhDM4YVeq8isDmbf3UuQogVe/dnXg03gYnP3.A4yHVyae.5E7C'
where username = 'admin';

update usuarios
set password = '$2a$10$khA/nF3DmnuixgYhX29PfuFh86ByViprAcz/AV5vW.a0qJBTWWzbu'
where username = 'vendedor';

update usuarios
set password = '$2a$10$nnDNtBkdUGvm2MAfLfwnu.prb.IHp5egkQt0CYY2wK7B/HW848vVW'
where username = 'almacen';

update usuarios
set password = '$2a$10$hOovCXa85djTmStMV.Fdk.uR2VfhCWcrgBbNiAfjWaWvZ7G99VLby'
where username = 'supervisor';

update usuarios
set password = '$2a$10$449UzqnONgBlmTHpAAkN2OrT5Plqct65KDCOHQXZ8l0kgDVt1hyme'
where username = 'inactivo';
